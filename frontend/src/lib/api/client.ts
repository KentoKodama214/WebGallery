/**
 * バックエンドAPIとの通信を管理するクライアントモジュール
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "";

/** メモリ上にアクセストークンを保持 */
let accessToken: string | null = null;

/**
 * 当該セッションの認証状態の推定値
 * - "anonymous" の場合、fetchWithAuthは先読みのリフレッシュを行わない
 */
let sessionAuthState: "unknown" | "authenticated" | "anonymous" = "unknown";

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
 * ログインAPIを呼び出す
 */
export async function login(
  accountId: string,
  password: string
): Promise<{ accessToken: string; expiresIn: number }> {
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

  const data = await response.json();
  accessToken = data.accessToken;
  sessionAuthState = "authenticated";
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
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // ネットワーク例外は一時的な失敗として扱い、セッション状態は変更しない
    accessToken = null;
    return false;
  }

  if (!response.ok) {
    accessToken = null;
    // 認証エラー（401/403）のみ未ログイン確定とする
    if (response.status === 401 || response.status === 403) {
      sessionAuthState = "anonymous";
    }
    return false;
  }

  try {
    const data = await response.json();
    accessToken = data.accessToken;
    sessionAuthState = "authenticated";
    return true;
  } catch {
    accessToken = null;
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
    if (refreshed) {
      headers.set("Authorization", `Bearer ${accessToken}`);
      response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers,
        credentials: "include",
      });
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
  const response = await fetch(
    `${API_BASE_URL}/api/v1/accounts?pageNo=${pageNo}`
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント一覧の取得に失敗しました"));
  }
  return response.json();
}

/**
 * アカウント詳細情報を取得する
 */
export async function getAccount(accountId: string): Promise<AccountDetail> {
  const response = await fetchWithAuth(`/api/v1/accounts/${accountId}`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント情報の取得に失敗しました"));
  }
  return response.json();
}

/**
 * アカウントを削除する
 */
export async function deleteAccount(accountId: string): Promise<void> {
  const response = await fetchWithAuth(`/api/v1/accounts/${accountId}`, {
    method: "DELETE",
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントの削除に失敗しました"));
  }
}

/**
 * アカウント情報を更新する
 */
export async function updateAccount(
  accountId: string,
  data: AccountUpdateData
): Promise<AccountUpdateResult> {
  const response = await fetchWithAuth(`/api/v1/accounts/${accountId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウント情報の更新に失敗しました"));
  }
  return response.json();
}

/**
 * 都道府県一覧を取得する
 */
export async function getPrefectures(): Promise<PrefectureGroup[]> {
  const response = await fetch(`${API_BASE_URL}/api/v1/prefectures`);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "都道府県一覧の取得に失敗しました"));
  }
  return response.json();
}

/**
 * アカウントを新規登録する
 */
export async function registerAccount(
  data: AccountRegistData
): Promise<AccountRegistResult> {
  const response = await fetch(`${API_BASE_URL}/api/v1/accounts`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントの登録に失敗しました"));
  }
  return response.json();
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
  const url = `/api/v1/accounts/${photoAccountId}/photos${query ? `?${query}` : ""}`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真一覧の取得に失敗しました"));
  }
  return response.json();
}

/**
 * 写真登録上限チェック
 */
export async function getPhotoUpperLimit(
  photoAccountId: string
): Promise<PhotoUpperLimitResponse> {
  const url = `/api/v1/accounts/${photoAccountId}/photos/upper-limit`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真登録上限の取得に失敗しました"));
  }
  return response.json();
}

/**
 * 写真詳細を取得する
 */
export async function getPhotoDetail(
  photoAccountId: string,
  accountNo: number,
  photoNo: number
): Promise<PhotoDetailResponse> {
  const url = `/api/v1/accounts/${photoAccountId}/photos/${photoNo}?accountNo=${accountNo}`;
  const response = await fetchWithAuth(url);
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真詳細の取得に失敗しました"));
  }
  return response.json();
}

/**
 * 写真を削除する
 */
export async function deletePhoto(
  photoAccountId: string,
  data: { photoNo: number; imageFilePath: string }
): Promise<PhotoEditResult> {
  const response = await fetchWithAuth(
    `/api/v1/accounts/${photoAccountId}/photos`,
    {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真の削除に失敗しました"));
  }
  return response.json();
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
  return response.json();
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
  return response.json();
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
    `/api/v1/accounts/${photoAccountId}/photos`,
    {
      method: isUpdate ? "PUT" : "POST",
      body: formData,
    }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "写真の保存に失敗しました"));
  }
  return response.json();
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
  return response.json();
}

/**
 * 管理者用アカウントロック解除
 */
export async function unlockAccount(
  accountNo: number
): Promise<AdminAccountLockResult> {
  const response = await fetchWithAuth(
    `/api/v1/admin/accounts/${accountNo}/unlock`,
    { method: "PUT" }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントのロック解除に失敗しました"));
  }
  return response.json();
}

/**
 * 管理者用アカウント強制ロック
 */
export async function lockAccount(
  accountNo: number
): Promise<AdminAccountLockResult> {
  const response = await fetchWithAuth(
    `/api/v1/admin/accounts/${accountNo}/lock`,
    { method: "PUT" }
  );
  if (!response.ok) {
    throw new Error(await readErrorMessage(response, "アカウントのロックに失敗しました"));
  }
  return response.json();
}
