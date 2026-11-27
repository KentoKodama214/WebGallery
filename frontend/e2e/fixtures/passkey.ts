import type { BrowserContext } from "@playwright/test";

/**
 * E2E 専用の擬似パスキー認証器
 *
 * ログインはパスキーのみで行うため、すべての E2E テストでパスキーの作成・署名が必要になる。
 * Chromium の仮想認証器（CDP の WebAuthn ドメイン）は WebKit（webkit-smoke・mobile-smoke）では使えないため、
 * ページ内の `navigator.credentials.create()` / `get()` を差し替え、WebCrypto で本物の ES256 鍵・署名と
 * attestation "none" の登録レスポンスを生成する。サーバー側の検証（origin・RP ID・チャレンジ・署名・UV）は
 * 本番と同じものを通る。
 *
 * 秘密鍵はワーカー（Node 側）のメモリに保持し、`exposeBinding` でページから読み書きする。
 * そのため同じワーカー内であれば、別のブラウザコンテキストから同じアカウントのパスキーでログインできる
 * （端末間で同期されたパスキーに相当する）。
 *
 * 実ブラウザの WebAuthn 実装を通す検証は、Chromium の仮想認証器を使う `scenarios/passkey_login.spec.ts` で行う。
 */

/** ワーカーが保持する擬似パスキー */
interface FakeCredential {
  /** クレデンシャルID（base64url） */
  id: string;
  rpId: string;
  /** ユーザーハンドル（WebAuthn の user.id。base64url） */
  userHandle: string;
  /** アカウントID（WebAuthn の user.name） */
  userName: string;
  privateKeyJwk: JsonWebKey;
  signCount: number;
  /** パスキーを作成した端末の名前（excludeCredentials の判定は端末単位で行う） */
  device: string;
}

/** 既定の端末の名前 */
const DEFAULT_DEVICE = "default";

const credentials: FakeCredential[] = [];

/** 次に作成するパスキーを保持する端末（別の端末でのパスキー追加を再現するために切り替える） */
let currentDevice = DEFAULT_DEVICE;

/** 次のログイン（アカウントを特定しない get()）で選ぶアカウントID。利用者がパスキーを選ぶ操作に相当する */
let selectedUserName: string | null = null;

/** 次の操作を利用者がキャンセルしたことにする（1回限り） */
let cancelNextOperation = false;

const installedContexts = new WeakSet<BrowserContext>();

/**
 * 次のパスキーログインで使うアカウントを選ぶ（パスキーの選択画面で選ぶ操作に相当する）
 *
 * @param accountId アカウントID
 */
export function selectPasskeyAccount(accountId: string): void {
  selectedUserName = accountId;
}

/**
 * 次に作成するパスキーを保持する端末を切り替える
 *
 * 認証器は自分が保持するパスキーが excludeCredentials に含まれると作成を拒否するため、
 * 同じアカウントに2つ目のパスキーを登録する操作は、別の端末に切り替えてから行う。
 * 署名（get）は端末を問わず、保持しているすべてのパスキーから選ぶ（同期パスキー相当）
 *
 * @param device 端末の名前（省略時は既定の端末に戻す）
 */
export function useFakeAuthenticatorDevice(device: string = DEFAULT_DEVICE): void {
  currentDevice = device;
}

/** 次のパスキー操作（作成・署名）を利用者がキャンセルしたことにする */
export function cancelNextPasskeyOperation(): void {
  cancelNextOperation = true;
}

/**
 * アカウントの擬似パスキーの件数を返す
 *
 * @param accountId アカウントID
 */
export function fakePasskeyCount(accountId: string): number {
  return credentials.filter((c) => c.userName === accountId).length;
}

/**
 * アカウントの擬似パスキーをすべて端末から消す（端末の紛失に相当する。サーバー側の登録は残る）
 *
 * @param accountId アカウントID
 */
export function forgetFakePasskeys(accountId: string): void {
  for (let i = credentials.length - 1; i >= 0; i--) {
    if (credentials[i].userName === accountId) credentials.splice(i, 1);
  }
}

/**
 * ブラウザコンテキストに擬似パスキー認証器を取り付ける（同じコンテキストへの2回目以降は何もしない）
 *
 * 取り付け後に読み込んだページから有効になるため、ページ遷移（`page.goto`）の前に呼ぶこと
 *
 * @param context 対象のブラウザコンテキスト
 */
export async function installFakeAuthenticator(context: BrowserContext): Promise<void> {
  if (installedContexts.has(context)) return;
  installedContexts.add(context);

  await context.exposeBinding("__e2ePasskeyState", () => {
    const cancelled = cancelNextOperation;
    cancelNextOperation = false;
    return { credentials, selectedUserName, cancelled, currentDevice };
  });
  await context.exposeBinding("__e2ePasskeySave", (_source, credential: FakeCredential) => {
    credentials.push(credential);
  });
  await context.exposeBinding(
    "__e2ePasskeyUpdateSignCount",
    (_source, id: string, signCount: number) => {
      const credential = credentials.find((c) => c.id === id);
      if (credential) credential.signCount = signCount;
    }
  );
  await context.addInitScript(fakeAuthenticatorScript);
}

/**
 * ページ内で `navigator.credentials` を差し替えるスクリプト（シリアライズしてページへ注入する）
 */
function fakeAuthenticatorScript(): void {
  type Bytes = Uint8Array<ArrayBuffer>;
  type State = {
    credentials: {
      id: string;
      rpId: string;
      userHandle: string;
      userName: string;
      privateKeyJwk: JsonWebKey;
      signCount: number;
      device: string;
    }[];
    selectedUserName: string | null;
    cancelled: boolean;
    currentDevice: string;
  };
  const w = window as unknown as {
    __e2ePasskeyState: () => Promise<State>;
    __e2ePasskeySave: (credential: State["credentials"][number]) => Promise<void>;
    __e2ePasskeyUpdateSignCount: (id: string, signCount: number) => Promise<void>;
  };
  if (!navigator.credentials || !window.isSecureContext) return;

  const toBytes = (value: BufferSource): Bytes =>
    Uint8Array.from(
      value instanceof ArrayBuffer
        ? new Uint8Array(value)
        : new Uint8Array(value.buffer, value.byteOffset, value.byteLength)
    );
  const toBase64Url = (bytes: Uint8Array): string => {
    let binary = "";
    bytes.forEach((b) => (binary += String.fromCharCode(b)));
    return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  };
  const fromBase64Url = (value: string): Bytes => {
    const base64 = value.replace(/-/g, "+").replace(/_/g, "/");
    const binary = atob(base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "="));
    return Uint8Array.from(binary, (c) => c.charCodeAt(0));
  };
  const concat = (...parts: Bytes[]): Bytes => {
    const out = new Uint8Array(parts.reduce((n, p) => n + p.length, 0));
    let offset = 0;
    for (const part of parts) {
      out.set(part, offset);
      offset += part.length;
    }
    return out;
  };
  const sha256 = async (data: Bytes): Promise<Bytes> =>
    new Uint8Array(await crypto.subtle.digest("SHA-256", data));
  const uint32 = (value: number): Bytes =>
    new Uint8Array([(value >>> 24) & 255, (value >>> 16) & 255, (value >>> 8) & 255, value & 255]);

  // 最小限の CBOR エンコーダ（attestationObject・COSE 公開鍵の生成に必要な型のみ）
  type Cbor = number | string | Bytes | Map<Cbor, Cbor>;
  const cborHeader = (major: number, length: number): Bytes => {
    if (length < 24) return new Uint8Array([(major << 5) | length]);
    if (length < 256) return new Uint8Array([(major << 5) | 24, length]);
    return new Uint8Array([(major << 5) | 25, (length >> 8) & 255, length & 255]);
  };
  const cbor = (value: Cbor): Bytes => {
    if (typeof value === "number") {
      return value >= 0 ? cborHeader(0, value) : cborHeader(1, -1 - value);
    }
    if (typeof value === "string") {
      const bytes = new TextEncoder().encode(value);
      return concat(cborHeader(3, bytes.length), bytes);
    }
    if (value instanceof Uint8Array) return concat(cborHeader(2, value.length), value);
    const entries = [...value.entries()].map(([k, v]) => concat(cbor(k), cbor(v)));
    return concat(cborHeader(5, value.size), ...entries);
  };

  // WebCrypto の ECDSA 署名（r||s の 64 バイト）を WebAuthn が求める DER 形式へ変換する
  const toDer = (raw: Bytes): Bytes => {
    const integer = (bytes: Bytes): Bytes => {
      let start = 0;
      while (start < bytes.length - 1 && bytes[start] === 0) start++;
      let body = bytes.slice(start);
      if (body[0] & 0x80) body = concat(new Uint8Array([0]), body);
      return concat(new Uint8Array([0x02, body.length]), body);
    };
    const sequence = concat(integer(raw.slice(0, 32)), integer(raw.slice(32)));
    return concat(new Uint8Array([0x30, sequence.length]), sequence);
  };

  const notAllowed = () =>
    new DOMException("The operation either timed out or was not allowed.", "NotAllowedError");

  // 署名・作成に時間のかかる実機を模して、わずかに待ってから応答する
  const settle = () => new Promise((resolve) => setTimeout(resolve, 50));

  const create = async (options: CredentialCreationOptions): Promise<Credential | null> => {
    const publicKey = options.publicKey;
    if (!publicKey) throw new DOMException("publicKey is required", "NotSupportedError");
    const state = await w.__e2ePasskeyState();
    await settle();
    if (state.cancelled) throw notAllowed();

    const rpId = publicKey.rp.id ?? location.hostname;
    const excluded = new Set(
      (publicKey.excludeCredentials ?? []).map((c) => toBase64Url(toBytes(c.id)))
    );
    if (
      state.credentials.some(
        (c) => c.device === state.currentDevice && c.rpId === rpId && excluded.has(c.id)
      )
    ) {
      throw new DOMException("The authenticator was previously registered.", "InvalidStateError");
    }

    const keyPair = (await crypto.subtle.generateKey(
      { name: "ECDSA", namedCurve: "P-256" },
      true,
      ["sign", "verify"]
    )) as CryptoKeyPair;
    const privateKeyJwk = await crypto.subtle.exportKey("jwk", keyPair.privateKey);
    const credentialId = crypto.getRandomValues(new Uint8Array(16));
    const id = toBase64Url(credentialId);

    const clientDataJSON = new TextEncoder().encode(
      JSON.stringify({
        type: "webauthn.create",
        challenge: toBase64Url(toBytes(publicKey.challenge)),
        origin: location.origin,
        crossOrigin: false,
      })
    );
    const cosePublicKey = cbor(
      new Map<Cbor, Cbor>([
        [1, 2],
        [3, -7],
        [-1, 1],
        [-2, fromBase64Url(privateKeyJwk.x!)],
        [-3, fromBase64Url(privateKeyJwk.y!)],
      ])
    );
    const authenticatorData = concat(
      await sha256(new TextEncoder().encode(rpId)),
      // UP（ユーザー在席）・UV（ユーザー検証）・AT（クレデンシャルデータあり）
      new Uint8Array([0x45]),
      uint32(0),
      new Uint8Array(16),
      new Uint8Array([(credentialId.length >> 8) & 255, credentialId.length & 255]),
      credentialId,
      cosePublicKey
    );
    const attestationObject = cbor(
      new Map<Cbor, Cbor>([
        ["fmt", "none"],
        ["attStmt", new Map()],
        ["authData", authenticatorData],
      ])
    );

    await w.__e2ePasskeySave({
      id,
      rpId,
      userHandle: toBase64Url(toBytes(publicKey.user.id)),
      userName: publicKey.user.name,
      privateKeyJwk,
      signCount: 0,
      device: state.currentDevice,
    });

    const response = {
      clientDataJSON: clientDataJSON.buffer,
      attestationObject: attestationObject.buffer,
      getTransports: () => ["internal"],
    };
    return {
      id,
      rawId: credentialId.buffer,
      type: "public-key",
      authenticatorAttachment: "platform",
      response,
      getClientExtensionResults: () => ({}),
      toJSON: () => ({
        id,
        rawId: id,
        type: "public-key",
        authenticatorAttachment: "platform",
        clientExtensionResults: {},
        response: {
          clientDataJSON: toBase64Url(clientDataJSON),
          attestationObject: toBase64Url(attestationObject),
          transports: ["internal"],
        },
      }),
    } as unknown as Credential;
  };

  const get = async (options: CredentialRequestOptions): Promise<Credential | null> => {
    const publicKey = options.publicKey;
    if (!publicKey) throw new DOMException("publicKey is required", "NotSupportedError");
    // オートフィル（Conditional UI）の待ち受けには応答しない（利用者が選ぶまで待ち続けるのと同じ）
    if (options.mediation === "conditional") {
      return new Promise((_resolve, reject) => {
        options.signal?.addEventListener("abort", () =>
          reject(new DOMException("aborted", "AbortError"))
        );
      });
    }
    const state = await w.__e2ePasskeyState();
    await settle();
    if (state.cancelled) throw notAllowed();

    const rpId = publicKey.rpId ?? location.hostname;
    const allowed = (publicKey.allowCredentials ?? []).map((c) => toBase64Url(toBytes(c.id)));
    const candidates = state.credentials.filter(
      (c) => c.rpId === rpId && (allowed.length === 0 || allowed.includes(c.id))
    );
    const selected = [...candidates]
      .reverse()
      .find((c) => c.userName === state.selectedUserName);
    // ログイン（allowCredentials なし）では、利用者が選んだアカウントのパスキーが端末に無ければ
    // 選べるものが無くキャンセルするのと同じ（NotAllowedError）。本人確認（allowCredentials あり）は
    // サーバーが本人のパスキーに限定しているため、選択が別アカウントのままでも許可されたものから選ぶ
    const credential =
      allowed.length === 0 && state.selectedUserName !== null
        ? selected
        : (selected ?? candidates[candidates.length - 1]);
    if (!credential) throw notAllowed();

    const signCount = credential.signCount + 1;
    await w.__e2ePasskeyUpdateSignCount(credential.id, signCount);

    const clientDataJSON = new TextEncoder().encode(
      JSON.stringify({
        type: "webauthn.get",
        challenge: toBase64Url(toBytes(publicKey.challenge)),
        origin: location.origin,
        crossOrigin: false,
      })
    );
    // UP（ユーザー在席）・UV（ユーザー検証）
    const authenticatorData = concat(
      await sha256(new TextEncoder().encode(rpId)),
      new Uint8Array([0x05]),
      uint32(signCount)
    );
    const privateKey = await crypto.subtle.importKey(
      "jwk",
      credential.privateKeyJwk,
      { name: "ECDSA", namedCurve: "P-256" },
      false,
      ["sign"]
    );
    const signature = toDer(
      new Uint8Array(
        await crypto.subtle.sign(
          { name: "ECDSA", hash: "SHA-256" },
          privateKey,
          concat(authenticatorData, await sha256(clientDataJSON))
        )
      )
    );
    const userHandle = fromBase64Url(credential.userHandle);
    const rawId = fromBase64Url(credential.id);

    return {
      id: credential.id,
      rawId: rawId.buffer,
      type: "public-key",
      authenticatorAttachment: "platform",
      response: {
        clientDataJSON: clientDataJSON.buffer,
        authenticatorData: authenticatorData.buffer,
        signature: signature.buffer,
        userHandle: userHandle.buffer,
      },
      getClientExtensionResults: () => ({}),
      toJSON: () => ({
        id: credential.id,
        rawId: credential.id,
        type: "public-key",
        authenticatorAttachment: "platform",
        clientExtensionResults: {},
        response: {
          clientDataJSON: toBase64Url(clientDataJSON),
          authenticatorData: toBase64Url(authenticatorData),
          signature: toBase64Url(signature),
          userHandle: credential.userHandle,
        },
      }),
    } as unknown as Credential;
  };

  // WebKit はインスタンスへの定義だけでは差し替わらないため、プロトタイプ（CredentialsContainer）側も差し替える
  for (const target of [navigator.credentials, Object.getPrototypeOf(navigator.credentials)]) {
    try {
      Object.defineProperty(target, "create", { configurable: true, writable: true, value: create });
      Object.defineProperty(target, "get", { configurable: true, writable: true, value: get });
    } catch {
      // 定義できない対象は無視する（もう一方で差し替わる）
    }
  }
}
