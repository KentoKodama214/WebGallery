/**
 * パスキー（WebAuthn）のブラウザ API を扱うヘルパー
 *
 * バックエンドは W3C WebAuthn の JSON 形式（`PublicKeyCredentialCreationOptionsJSON` /
 * `PublicKeyCredentialRequestOptionsJSON`）でオプションを返し、`PublicKeyCredential#toJSON()` の形式で
 * レスポンスを受け取る。`PublicKeyCredential.parseCreationOptionsFromJSON()` 等の新しい API を
 * 持たないブラウザもあるため、base64url とバイナリの相互変換はここで行う（外部ライブラリには依存しない）。
 */

/** パスキー登録オプション（PublicKeyCredentialCreationOptionsJSON） */
export interface PasskeyRegistrationOptionsJSON {
  rp: { id: string; name: string };
  user: { id: string; name: string; displayName: string };
  challenge: string;
  pubKeyCredParams: { type: "public-key"; alg: number }[];
  timeout?: number;
  excludeCredentials?: { id: string; type: "public-key" }[];
  authenticatorSelection?: {
    residentKey?: ResidentKeyRequirement;
    requireResidentKey?: boolean;
    userVerification?: UserVerificationRequirement;
  };
  attestation?: AttestationConveyancePreference;
}

/** パスキー認証オプション（PublicKeyCredentialRequestOptionsJSON） */
export interface PasskeyAuthenticationOptionsJSON {
  challenge: string;
  timeout?: number;
  rpId?: string;
  allowCredentials?: { id: string; type: "public-key" }[];
  userVerification?: UserVerificationRequirement;
}

/**
 * パスキー操作がユーザーにより中断された（キャンセル・タイムアウト・別の操作による中止）ことを表すエラー
 *
 * 画面では失敗として赤字表示せず、静かに元の状態へ戻すために区別する
 */
export class PasskeyCancelledError extends Error {
  constructor(message = "パスキーの操作がキャンセルされました") {
    super(message);
    this.name = "PasskeyCancelledError";
  }
}

/**
 * このブラウザでパスキー（WebAuthn）が使えるかどうか
 *
 * WebAuthn は安全なコンテキスト（HTTPS または localhost）でのみ利用できる
 */
export function isPasskeySupported(): boolean {
  return (
    typeof window !== "undefined" &&
    typeof window.PublicKeyCredential === "function" &&
    typeof navigator !== "undefined" &&
    !!navigator.credentials
  );
}

/**
 * ユーザー名欄のオートフィルからパスキーを選べる（Conditional UI）かどうか
 */
export async function isConditionalMediationAvailable(): Promise<boolean> {
  if (!isPasskeySupported()) return false;
  const pkc = window.PublicKeyCredential as typeof PublicKeyCredential & {
    isConditionalMediationAvailable?: () => Promise<boolean>;
  };
  if (typeof pkc.isConditionalMediationAvailable !== "function") return false;
  try {
    return await pkc.isConditionalMediationAvailable();
  } catch {
    return false;
  }
}

/**
 * base64url 文字列をバイナリに変換する
 *
 * @param value base64url 文字列（パディングなし）
 * @returns バイナリ
 */
export function base64UrlToBuffer(value: string): ArrayBuffer {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
  const binary = atob(padded);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

/**
 * バイナリを base64url 文字列に変換する
 *
 * @param value バイナリ
 * @returns base64url 文字列（パディングなし）
 */
export function bufferToBase64Url(value: ArrayBuffer | ArrayBufferView): string {
  const bytes =
    value instanceof ArrayBuffer
      ? new Uint8Array(value)
      : new Uint8Array(value.buffer, value.byteOffset, value.byteLength);
  let binary = "";
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

/**
 * 登録オプションの JSON を `navigator.credentials.create()` に渡せる形へ変換する
 */
export function toCreationOptions(
  json: PasskeyRegistrationOptionsJSON
): PublicKeyCredentialCreationOptions {
  return {
    rp: json.rp,
    user: {
      id: base64UrlToBuffer(json.user.id),
      name: json.user.name,
      displayName: json.user.displayName,
    },
    challenge: base64UrlToBuffer(json.challenge),
    pubKeyCredParams: json.pubKeyCredParams,
    timeout: json.timeout,
    excludeCredentials: (json.excludeCredentials ?? []).map((c) => ({
      id: base64UrlToBuffer(c.id),
      type: c.type,
    })),
    authenticatorSelection: json.authenticatorSelection,
    attestation: json.attestation,
  };
}

/**
 * 認証オプションの JSON を `navigator.credentials.get()` に渡せる形へ変換する
 */
export function toRequestOptions(
  json: PasskeyAuthenticationOptionsJSON
): PublicKeyCredentialRequestOptions {
  return {
    challenge: base64UrlToBuffer(json.challenge),
    timeout: json.timeout,
    rpId: json.rpId,
    allowCredentials: (json.allowCredentials ?? []).map((c) => ({
      id: base64UrlToBuffer(c.id),
      type: c.type,
    })),
    userVerification: json.userVerification,
  };
}

/**
 * 認証器が返した `PublicKeyCredential` を `toJSON()` の形式の文字列にする
 *
 * ブラウザが `toJSON()` を実装していればそれを使い、無ければ同じ形式を組み立てる
 *
 * @param credential `navigator.credentials.create()` / `get()` の戻り値
 * @returns JSON 文字列（バックエンドへそのまま送る）
 */
export function credentialToJSON(credential: PublicKeyCredential): string {
  const withToJSON = credential as PublicKeyCredential & { toJSON?: () => unknown };
  if (typeof withToJSON.toJSON === "function") {
    return JSON.stringify(withToJSON.toJSON());
  }

  const response = credential.response;
  const base = {
    id: credential.id,
    rawId: bufferToBase64Url(credential.rawId),
    type: credential.type,
    clientExtensionResults: credential.getClientExtensionResults?.() ?? {},
    authenticatorAttachment: credential.authenticatorAttachment ?? undefined,
  };
  if ("attestationObject" in response) {
    const attestation = response as AuthenticatorAttestationResponse;
    return JSON.stringify({
      ...base,
      response: {
        clientDataJSON: bufferToBase64Url(attestation.clientDataJSON),
        attestationObject: bufferToBase64Url(attestation.attestationObject),
        transports:
          typeof attestation.getTransports === "function" ? attestation.getTransports() : [],
      },
    });
  }
  const assertion = response as AuthenticatorAssertionResponse;
  return JSON.stringify({
    ...base,
    response: {
      clientDataJSON: bufferToBase64Url(assertion.clientDataJSON),
      authenticatorData: bufferToBase64Url(assertion.authenticatorData),
      signature: bufferToBase64Url(assertion.signature),
      userHandle: assertion.userHandle ? bufferToBase64Url(assertion.userHandle) : undefined,
    },
  });
}

/**
 * ブラウザの WebAuthn 例外を、画面に出せるメッセージへ変換する
 *
 * @param error 例外
 * @param fallback 既定のメッセージ
 * @returns 変換後のエラー（キャンセル系は {@link PasskeyCancelledError}）
 */
function toPasskeyError(error: unknown, fallback: string): Error {
  const name = error instanceof DOMException || error instanceof Error ? error.name : "";
  // NotAllowedError：ユーザーのキャンセル・タイムアウト（原因を区別する手段はない）
  // AbortError：AbortController による中止（オートフィル待ちの中止など）
  if (name === "NotAllowedError" || name === "AbortError") {
    return new PasskeyCancelledError();
  }
  // InvalidStateError：excludeCredentials に含まれる（この端末のパスキーは登録済み）
  if (name === "InvalidStateError") {
    return new Error("この端末のパスキーは既に登録されています");
  }
  // SecurityError：RP ID とページのオリジンが合わない（設定不備）
  if (name === "SecurityError") {
    return new Error("このページではパスキーを利用できません");
  }
  return new Error(fallback);
}

/**
 * 端末でパスキーを作成し、登録レスポンスの JSON 文字列を返す
 *
 * @param options バックエンドが発行した登録オプション
 * @returns 登録レスポンス（`PublicKeyCredential#toJSON()` の形式の JSON 文字列）
 */
export async function createPasskeyCredential(
  options: PasskeyRegistrationOptionsJSON
): Promise<string> {
  if (!isPasskeySupported()) {
    throw new Error("このブラウザはパスキーに対応していません");
  }
  let credential: Credential | null;
  try {
    credential = await navigator.credentials.create({ publicKey: toCreationOptions(options) });
  } catch (error) {
    throw toPasskeyError(error, "パスキーの作成に失敗しました");
  }
  if (!credential) {
    throw new PasskeyCancelledError();
  }
  return credentialToJSON(credential as PublicKeyCredential);
}

/**
 * 端末のパスキーで署名し、認証レスポンスの JSON 文字列を返す
 *
 * @param options バックエンドが発行した認証オプション
 * @param conditional ユーザー名欄のオートフィルからの選択を待つ（Conditional UI）場合 true
 * @param signal 待機を中止するためのシグナル
 * @returns 認証レスポンス（`PublicKeyCredential#toJSON()` の形式の JSON 文字列）
 */
export async function getPasskeyCredential(
  options: PasskeyAuthenticationOptionsJSON,
  { conditional = false, signal }: { conditional?: boolean; signal?: AbortSignal } = {}
): Promise<string> {
  if (!isPasskeySupported()) {
    throw new Error("このブラウザはパスキーに対応していません");
  }
  let credential: Credential | null;
  try {
    credential = await navigator.credentials.get({
      publicKey: toRequestOptions(options),
      ...(conditional ? { mediation: "conditional" as CredentialMediationRequirement } : {}),
      signal,
    });
  } catch (error) {
    throw toPasskeyError(error, "パスキーによる認証に失敗しました");
  }
  if (!credential) {
    throw new PasskeyCancelledError();
  }
  return credentialToJSON(credential as PublicKeyCredential);
}
