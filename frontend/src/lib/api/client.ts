/**
 * バックエンドAPIとの通信を管理するクライアントモジュール
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "";

/**
 * API 呼び出しが HTTP エラーを返したことを表すエラー
 *
 * 呼び出し側が HTTP ステータスに応じて分岐（例：403 の再認証失敗を数える）できるよう、
 * メッセージに加えて `status` を保持する。
 */
export class ApiError extends Error {
  readonly status: number;
  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

/**
 * パスセグメントを URL エンコードする
 *
 * 経路上の `app/api/[...path]/route.ts` でもドットセグメント等は拒否されるが、
 * 多層防御としてクライアント側でもパスパラメータをエスケープする。
 */
const seg = (value: string | number): string => encodeURIComponent(String(value));

/** メモリ上にアクセストークンを保持 */
let accessToken: string | null = null;

/**
 * 当該セッションの認証状態の推定値
 * - "anonymous" の場合、fetchWithAuthは先読みのリフレッシュを行わない
 */
let sessionAuthState: "unknown" | "authenticated" | "anonymous" = "unknown";

/**
 * 認証状態の世代番号
 *
 * login / logout のたびにインクリメントする。進行中のリフレッシュ処理は開始時点の
 * 世代を記憶し、応答適用前に世代が変わっていたら結果を破棄する。これにより
 * 「マウント時の先読みリフレッシュ応答が、その後に成立したログインのトークンや
 * セッション状態を上書きする」競合を防ぐ。
 */
let authEpoch = 0;

/**
 * 直近でリフレッシュが「一時的な失敗」（ネットワーク例外・5xx）で終わった時刻。
 * バックエンド断・オフライン時に画面遷移のたびリフレッシュ要求が飛ぶのを防ぐため、
 * この時刻から {@link REFRESH_RETRY_COOLDOWN_MS} の間は再試行をスキップする。
 * 明確な認証エラー（401/403）や成功・login/logout ではリセットされる。
 */
let lastRefreshTransientFailureAt = 0;

/** 一時的な失敗後にリフレッシュ再試行を抑止するクールダウン（ミリ秒） */
const REFRESH_RETRY_COOLDOWN_MS = 5_000;

/**
 * レスポンスボディからエラーメッセージを取り出す
 *
 * 5xx（サーバー内部エラー）は内部的な例外メッセージが含まれ得るため既定文言を返す。
 * 4xx（バリデーション・認証エラー等）はユーザー向けのメッセージとして採用する。
 *
 * @param response fetchのレスポンス
 * @param fallback 取り出せなかった場合の既定メッセージ
 * @returns エラーメッセージ
 */
async function readErrorMessage(
  response: Response,
  fallback: string
): Promise<string> {
  if (response.status >= 500) return fallback;
  try {
    const text = await response.text();
    if (!text) return fallback;
    try {
      const body = JSON.parse(text);
      return body.errorMessage || body.message || fallback;
    } catch {
      return fallback;
    }
  } catch {
    return fallback;
  }
}

/**
 * レスポンスボディを JSON として読み取る
 *
 * 2xx でも本文が空・非 JSON の場合に生の SyntaxError がそのまま UI に
 * 表示されるのを防ぎ、既定文言へフォールバックする。
 *
 * @param response fetch のレスポンス
 * @returns パース済み JSON
 */
async function readJson<T>(response: Response): Promise<T> {
  try {
    return (await response.json()) as T;
  } catch {
    throw new Error(
      "サーバーからの応答を解釈できませんでした。時間をおいて再度お試しください"
    );
  }
}

/**
 * アクセストークンを取得する
 */
export function getAccessToken(): string | null {
  return accessToken;
}

/**
 * アクセストークンを設定する
 */
export function setAccessToken(token: string | null): void {
  accessToken = token;
}

/**
 * 認証状態を未ログイン確定へ完全にリセットする
 *
 * `accessToken` だけでなく `sessionAuthState` と `authEpoch` もまとめて更新する。
 * `login()` が途中まで進んだ後にトークン解釈へ失敗した場合など、
 * 「トークンは null だがセッション状態は authenticated のまま」という不整合を防ぐ。
 * 進行中のリフレッシュ応答は世代不一致で破棄される。
 */
export function clearAuthState(): void {
  accessToken = null;
  sessionAuthState = "anonymous";
  lastRefreshTransientFailureAt = 0;
  authEpoch++;
}

/**
 * ログインAPIを呼び出す
 */
export async function login(
  accountId: string,
  password: string
): Promise<{ accessToken: string; expiresIn: number }> {
  // 認証の起点となる呼び出しのため、意図的に生 fetch を使う
  // （fetchWithAuth はトークン前提でリフレッシュを試みてしまう）
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ accountId, password }),
    });
  } catch {
    throw new Error("サーバーに接続できませんでした。時間をおいて再度お試しください");
  }

  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "ログインに失敗しました"));
  }

  const data = await readJson<{ accessToken: string; expiresIn: number }>(
    response
  );
  // 応答に文字列のアクセストークンが含まれない場合はログイン失敗として扱う
  // （壊れた応答で「認証済みだがトークン無し」状態に陥るのを防ぐ）
  if (typeof data.accessToken !== "string" || data.accessToken === "") {
    throw new Error(
      "サーバーからの応答を解釈できませんでした。時間をおいて再度お試しください"
    );
  }
  // 先にトークンを確定させてから世代を進める。これ以降、開始済みのリフレッシュ
  // 応答は世代不一致で破棄され、このログイン結果を上書きできない。
  accessToken = data.accessToken;
  sessionAuthState = "authenticated";
  lastRefreshTransientFailureAt = 0;
  authEpoch++;
  return data;
}

/**
 * 進行中のリフレッシュ処理を共有するためのプロミス
 * （同時に複数のリクエストがリフレッシュを要求してもAPI呼び出しは1回にまとめる）
 */
let refreshInFlight: Promise<boolean> | null = null;

/**
 * リフレッシュトークンを使ってアクセストークンを更新する
 *
 * 同時呼び出しは進行中の1回にまとめる（シングルフライト）。
 * リフレッシュトークンをローテーションするバックエンドで、並行した複数の
 * リフレッシュにより後発のリクエストが無効化される競合を避ける。
 *
 * セッション状態を "anonymous"（未ログイン確定）へ落とすのは、リフレッシュAPIが
 * 明確に 401/403 を返した場合のみ。ネットワーク例外や 5xx などの一時的な失敗では
 * セッション状態を変更せず、次回のリクエストで再試行できるようにする。
 */
export function refresh(): Promise<boolean> {
  // 直近の一時的な失敗から日が浅い場合は、無駄な再試行でバックエンドを叩かない
  if (
    !refreshInFlight &&
    lastRefreshTransientFailureAt !== 0 &&
    Date.now() - lastRefreshTransientFailureAt < REFRESH_RETRY_COOLDOWN_MS
  ) {
    return Promise.resolve(false);
  }
  if (!refreshInFlight) {
    refreshInFlight = doRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

/**
 * リフレッシュAPIを実際に呼び出す（{@link refresh} からのみ利用する）
 */
async function doRefresh(): Promise<boolean> {
  // このリフレッシュ処理の開始時点の世代。応答適用前に login/logout が
  // 起きていたら（世代不一致）グローバルな状態は書き換えない。
  const epoch = authEpoch;
  const isStale = () => authEpoch !== epoch;

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // ネットワーク例外は一時的な失敗として扱い、セッション状態は変更しない
    if (!isStale()) {
      accessToken = null;
      lastRefreshTransientFailureAt = Date.now();
    }
    return false;
  }

  if (!response.ok) {
    if (isStale()) return false;
    accessToken = null;
    // 認証エラー（401/403）のみ未ログイン確定とする。5xx 等は一時的失敗として
    // クールダウンを設定し、次回以降の即時再試行を抑止する。
    if (response.status === 401 || response.status === 403) {
      sessionAuthState = "anonymous";
      lastRefreshTransientFailureAt = 0;
    } else {
      lastRefreshTransientFailureAt = Date.now();
    }
    return false;
  }

  try {
    const data = await response.json();
    if (isStale()) return false;
    // 文字列のアクセストークンが得られない応答は失敗扱いにする
    // （`sessionAuthState="authenticated"` かつトークン無しでリフレッシュが
    //   永久にスキップされる不整合を防ぐ）
    if (typeof data?.accessToken !== "string" || data.accessToken === "") {
      accessToken = null;
      return false;
    }
    accessToken = data.accessToken;
    sessionAuthState = "authenticated";
    lastRefreshTransientFailureAt = 0;
    return true;
  } catch {
    if (!isStale()) accessToken = null;
    return false;
  }
}

/**
 * ログアウトAPIを呼び出す
 */
export async function logout(): Promise<void> {
  try {
    await fetch(`${API_BASE_URL}/api/v1/auth/logout`, {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // ログアウトAPIの失敗は無視し、クライアント側の状態だけクリアする
  }
  accessToken = null;
  sessionAuthState = "anonymous";
  lastRefreshTransientFailureAt = 0;
  // 進行中のリフレッシュ応答がログアウト後の状態を上書きしないよう世代を進める
  authEpoch++;
}

/**
 * 認証付きfetchを行う（401時に自動リフレッシュ+リトライ）
 */
export async function fetchWithAuth(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");
  // AuthProvider のマウント時に開始されたセッション復元リフレッシュが進行中なら、
  // その結果（accessToken / sessionAuthState）を先に反映させてから判断する。
  // これにより sessionAuthState が "unknown" のまま独自にもう一度 refresh を呼ぶ二重処理を避ける。
  if (refreshInFlight) {
    await refreshInFlight;
  }
  // 未ログインが確定している場合は無駄なリフレッシュ試行を行わない
  if (!accessToken && sessionAuthState !== "anonymous") {
    await refresh();
  }
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  let response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers,
    credentials: "include",
  });

  if (response.status === 401) {
    const refreshed = await refresh();
    // await 後に別コンテキストの logout/clearAuthState でトークンが消えている
    // 可能性があるため、グローバルではなくこの時点の値を控えて使う（`Bearer null` 送出防止）
    const refreshedToken = accessToken;
    if (refreshed && refreshedToken) {
      headers.set("Authorization", `Bearer ${refreshedToken}`);
      response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
        credentials: "include",
      });
      // リフレッシュ直後の再リクエストも 401 の場合、認証は実効的に失効している。
      // 認証状態を未ログイン確定へ倒し、以降のガード付きページで /login へ
      // 誘導できるようにする（汎用エラーのまま袋小路になるのを防ぐ）。
      if (response.status === 401) {
        clearAuthState();
      }
    }
  }

  return response;
}

/** アカウント一覧レスポンス（バックエンドはページング済みの結果を返す） */
export interface AccountListGetResponse {
  isLast: boolean;
  accountList: AccountListItem[];
}

/**
 * アカウント一覧を1ページ分取得する
 */
export async function getAccountList(pageNo: number = 1): Promise<AccountListGetResponse> {
  // 現状この一覧は公開 API だが、将来保護 API 化しても静かに壊れないよう
  // 認証付き fetch を通す（未ログイン時も追加のリフレッシュ試行が走るだけで害はない）
  const response = await fetchWithAuth(`/api/v1/accounts?pageNo=${pageNo}`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント一覧の取得に失敗しました"));
  }
  return readJson<AccountListGetResponse>(response);
}

/**
 * アカウント詳細情報を取得する
 */
export async function getAccount(accountId: string): Promise<AccountDetail> {
  const response = await fetchWithAuth(`/api/v1/accounts/${seg(accountId)}`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント情報の取得に失敗しました"));
  }
  return readJson<AccountDetail>(response);
}

/**
 * アカウントを削除する
 *
 * 本人確認のための現在のパスワードはリクエストボディ（JSON）で送る。
 * DELETE のボディは一部の CDN・リバースプロキシ・WAF で破棄されうること、および
 * 現在のパスワードのような機微情報をアクセスログ等に記録されやすいカスタムヘッダーに
 * 載せないことの両方を満たすため、`POST /api/v1/accounts/{id}/deletion` を呼び出す。
 */
export async function deleteAccount(
  accountId: string,
  currentPassword: string
): Promise<void> {
  const response = await fetchWithAuth(
    `/api/v1/accounts/${seg(accountId)}/deletion`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ currentPassword }),
    }
  );
  if (!response.ok) {
    throw new ApiError(
      await readErrorMessage(response, "アカウントの削除に失敗しました"),
      response.status
    );
  }
}

/**
 * アカウント情報を更新する
 */
export async function updateAccount(
  accountId: string,
  data: AccountUpdateData
): Promise<AccountUpdateResult> {
  const response = await fetchWithAuth(`/api/v1/accounts/${seg(accountId)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new ApiError(
      await readErrorMessage(response, "アカウント情報の更新に失敗しました"),
      response.status
    );
  }
  return readJson<AccountUpdateResult>(response);
}

/**
 * 都道府県一覧を取得する
 */
export async function getPrefectures(): Promise<PrefectureGroup[]> {
  // 認証済み画面（アカウント設定）からも呼ばれる。将来の保護 API 化に備え認証付き fetch を通す
  const response = await fetchWithAuth(`/api/v1/prefectures`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "都道府県一覧の取得に失敗しました"));
  }
  return readJson<PrefectureGroup[]>(response);
}

/**
 * アカウントを新規登録する
 */
export async function registerAccount(
  data: AccountRegistData
): Promise<AccountRegistResult> {
  // 未ログインのユーザーが行う登録のため、意図的に生 fetch を使う（認証不要）
  const response = await fetch(`${API_BASE_URL}/api/v1/accounts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントの登録に失敗しました"));
  }
  return readJson<AccountRegistResult>(response);
}

/** アカウント一覧アイテム */
export interface AccountListItem {
  accountId: string;
  accountName: string;
}

/** アカウント登録リクエスト */
export interface AccountRegistData {
  accountId: string;
  accountName: string;
  password: string;
  birthdate: string | null;
  sexKbn: string;
  birthplacePrefectureKbnCode: string;
  residentPrefectureKbnCode: string;
  freeMemo: string;
}

/** アカウント登録結果 */
export interface AccountRegistResult {
  httpStatus: number;
  isSuccess: boolean;
  message: string;
}

/** アカウント詳細情報 */
export interface AccountDetail {
  accountId: string;
  accountName: string;
  birthdate: string | null;
  sexKbn: string;
  birthplacePrefectureKbnCode: string;
  residentPrefectureKbnCode: string;
  freeMemo: string;
}

/** アカウント更新リクエスト */
export interface AccountUpdateData {
  accountId: string;
  accountName: string;
  newPassword: string;
  /** 現在のパスワード（パスワード変更時のみ必須。それ以外は空文字） */
  currentPassword: string;
  birthdate: string | null;
  sexKbn: string;
  birthplacePrefectureKbnCode: string;
  residentPrefectureKbnCode: string;
  freeMemo: string;
}

/** アカウント更新結果 */
export interface AccountUpdateResult {
  httpStatus: number;
  isDuplicateAccountId: boolean;
  isAccountIdChanged: boolean;
  isPasswordChanged: boolean;
  message: string;
}

/** 都道府県グループ */
export interface PrefectureGroup {
  groupName: string;
  prefectures: Prefecture[];
}

/** 都道府県 */
export interface Prefecture {
  kbnCode: string;
  kbnJapaneseName: string;
}

/** 写真タグアイテム */
export interface PhotoTagItem {
  accountNo: number;
  photoNo: number;
  tagNo: number;
  tagJapaneseName: string;
  tagEnglishName: string;
}

/** 写真詳細レスポンス */
export interface PhotoDetailResponse {
  accountNo: number;
  photoNo: number;
  isFavorite: boolean;
  photoAt: string | null;
  locationNo: number | null;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  locationName: string | null;
  imageFilePath: string;
  photoJapaneseTitle: string | null;
  photoEnglishTitle: string | null;
  caption: string | null;
  directionKbn: string | null;
  focalLength: number | null;
  fValue: number | null;
  shutterSpeed: number | null;
  iso: number | null;
  photoTagList: PhotoTagItem[];
}

/** 写真編集結果レスポンス */
export interface PhotoEditResult {
  httpStatus: number;
  isSuccess: boolean;
  message: string;
  photoNo: number;
  imageFilePath: string;
}

/** お気に入り操作結果レスポンス */
export interface PhotoFavoriteResult {
  httpStatus: number;
  isSuccess: boolean;
  message: string;
}

/** 写真一覧アイテム */
export interface PhotoListItem {
  accountNo: number;
  photoNo: number;
  isFavorite: boolean;
  imageFilePath: string;
  caption: string;
  directionKbn: string;
}

/** 写真一覧レスポンス */
export interface PhotoListResponse {
  isLast: boolean;
  photoList: PhotoListItem[];
}

/** 写真登録上限チェックレスポンス */
export interface PhotoUpperLimitResponse {
  isReachedUpperLimit: boolean;
}

/** 写真一覧取得パラメータ */
export interface PhotoListParams {
  directionKbn?: string;
  isFavorite?: string;
  tagList?: string;
  sortBy?: string;
  pageNo?: number;
}

/**
 * 写真一覧を取得する
 */
export async function getPhotoList(
  photoAccountId: string,
  params: PhotoListParams = {}
): Promise<PhotoListResponse> {
  const searchParams = new URLSearchParams();
  if (params.directionKbn) searchParams.set("directionKbn", params.directionKbn);
  if (params.isFavorite) searchParams.set("isFavorite", params.isFavorite);
  if (params.tagList) searchParams.set("tagList", params.tagList);
  if (params.sortBy) searchParams.set("sortBy", params.sortBy);
  if (params.pageNo !== undefined) searchParams.set("pageNo", String(params.pageNo));

  const query = searchParams.toString();
  const url = `/api/v1/accounts/${seg(photoAccountId)}/photos${query ? `?${query}` : ""}`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真一覧の取得に失敗しました"));
  }
  return readJson<PhotoListResponse>(response);
}

/**
 * 写真登録上限チェック
 */
export async function getPhotoUpperLimit(
  photoAccountId: string
): Promise<PhotoUpperLimitResponse> {
  const url = `/api/v1/accounts/${seg(photoAccountId)}/photos/upper-limit`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真登録上限の取得に失敗しました"));
  }
  return readJson<PhotoUpperLimitResponse>(response);
}

/**
 * 写真詳細を取得する
 */
export async function getPhotoDetail(
  photoAccountId: string,
  photoNo: number
): Promise<PhotoDetailResponse> {
  // バックエンドは写真の所有者を photoAccountId（パス）で解決する。
  // お気に入り判定に使うアカウント番号はセッション（JWT）から取得されるため、
  // クライアントからアカウント番号を渡す必要はない
  const url = `/api/v1/accounts/${seg(photoAccountId)}/photos/${seg(photoNo)}`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真詳細の取得に失敗しました"));
  }
  return readJson<PhotoDetailResponse>(response);
}

/**
 * 写真を削除する
 */
export async function deletePhoto(
  photoAccountId: string,
  data: { photoNo: number; imageFilePath: string }
): Promise<PhotoEditResult> {
  const response = await fetchWithAuth(
    `/api/v1/accounts/${seg(photoAccountId)}/photos`,
    {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真の削除に失敗しました"));
  }
  return readJson<PhotoEditResult>(response);
}

/**
 * お気に入りを登録する
 */
export async function addFavorite(
  favoritePhotoAccountNo: number,
  favoritePhotoNo: number
): Promise<PhotoFavoriteResult> {
  const response = await fetchWithAuth("/api/v1/photos/favorites", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ favoritePhotoAccountNo, favoritePhotoNo }),
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "お気に入りの登録に失敗しました"));
  }
  return readJson<PhotoFavoriteResult>(response);
}

/**
 * お気に入りを解除する
 */
export async function deleteFavorite(
  favoritePhotoAccountNo: number,
  favoritePhotoNo: number
): Promise<PhotoFavoriteResult> {
  const response = await fetchWithAuth("/api/v1/photos/favorites", {
    method: "DELETE",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ favoritePhotoAccountNo, favoritePhotoNo }),
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "お気に入りの解除に失敗しました"));
  }
  return readJson<PhotoFavoriteResult>(response);
}

/**
 * 写真を保存する（新規登録・更新）
 */
export async function savePhoto(
  photoAccountId: string,
  formData: FormData,
  isUpdate: boolean
): Promise<PhotoEditResult> {
  const response = await fetchWithAuth(
    `/api/v1/accounts/${seg(photoAccountId)}/photos`,
    {
      method: isUpdate ? "PUT" : "POST",
      body: formData,
    }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真の保存に失敗しました"));
  }
  return readJson<PhotoEditResult>(response);
}

/** 管理者用アカウント一覧アイテム */
export interface AdminAccountListItem {
  accountNo: number;
  accountId: string;
  accountName: string;
  authorityKbn: string;
  isDeleted: boolean;
  lastLoginDatetime: string | null;
  loginFailureCount: number;
}

/** 管理者用アカウントロック操作結果 */
export interface AdminAccountLockResult {
  httpStatus: number;
  isSuccess: boolean;
  message: string;
}

/** 管理者用アカウント一覧レスポンス（バックエンドはページング済みの結果を返す） */
export interface AdminAccountListGetResponse {
  isLast: boolean;
  accountList: AdminAccountListItem[];
}

/**
 * 管理者用アカウント一覧を1ページ分取得する
 */
export async function getAdminAccountList(pageNo: number = 1): Promise<AdminAccountListGetResponse> {
  const response = await fetchWithAuth(`/api/v1/admin/accounts?pageNo=${pageNo}`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント一覧の取得に失敗しました"));
  }
  return readJson<AdminAccountListGetResponse>(response);
}

/**
 * 管理者用アカウントロック解除
 */
export async function unlockAccount(
  accountNo: number
): Promise<AdminAccountLockResult> {
  const response = await fetchWithAuth(
    `/api/v1/admin/accounts/${seg(accountNo)}/unlock`,
    { method: "PUT" }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントのロック解除に失敗しました"));
  }
  return readJson<AdminAccountLockResult>(response);
}

/**
 * 管理者用アカウント強制ロック
 */
export async function lockAccount(
  accountNo: number
): Promise<AdminAccountLockResult> {
  const response = await fetchWithAuth(
    `/api/v1/admin/accounts/${seg(accountNo)}/lock`,
    { method: "PUT" }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントのロックに失敗しました"));
  }
  return readJson<AdminAccountLockResult>(response);
}
