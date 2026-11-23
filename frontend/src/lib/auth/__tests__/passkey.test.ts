/**
 * パスキー（WebAuthn）ヘルパーの単体テスト
 */

import {
  PasskeyCancelledError,
  base64UrlToBuffer,
  bufferToBase64Url,
  createPasskeyCredential,
  credentialToJSON,
  getPasskeyCredential,
  isPasskeySupported,
  toCreationOptions,
  toRequestOptions,
} from "../passkey";
import type {
  PasskeyAuthenticationOptionsJSON,
  PasskeyRegistrationOptionsJSON,
} from "../passkey";

const REGISTRATION_OPTIONS: PasskeyRegistrationOptionsJSON = {
  rp: { id: "localhost", name: "WebGallery" },
  user: { id: "AQID", name: "testuser1", displayName: "テストユーザー" },
  challenge: "BAUG",
  pubKeyCredParams: [{ type: "public-key", alg: -7 }],
  timeout: 300000,
  excludeCredentials: [{ id: "BwgJ", type: "public-key" }],
  authenticatorSelection: {
    residentKey: "required",
    requireResidentKey: true,
    userVerification: "required",
  },
  attestation: "none",
};

const AUTHENTICATION_OPTIONS: PasskeyAuthenticationOptionsJSON = {
  challenge: "BAUG",
  timeout: 300000,
  rpId: "localhost",
  allowCredentials: [],
  userVerification: "required",
};

/** Uint8Array を比較しやすい配列にする */
const bytes = (buffer: BufferSource | ArrayBuffer) =>
  Array.from(
    buffer instanceof ArrayBuffer
      ? new Uint8Array(buffer)
      : new Uint8Array(
          (buffer as ArrayBufferView).buffer,
          (buffer as ArrayBufferView).byteOffset,
          (buffer as ArrayBufferView).byteLength
        )
  );

/** 認証器のエラーを模倣した DOMException を生成する */
const domError = (name: string) => new DOMException("error", name);

describe("lib/auth/passkey", () => {
  const originalCredentials = Object.getOwnPropertyDescriptor(navigator, "credentials");
  const originalPkc = (window as unknown as { PublicKeyCredential?: unknown }).PublicKeyCredential;
  let createMock: jest.Mock;
  let getMock: jest.Mock;

  beforeEach(() => {
    createMock = jest.fn();
    getMock = jest.fn();
    Object.defineProperty(navigator, "credentials", {
      configurable: true,
      value: { create: createMock, get: getMock },
    });
    (window as unknown as { PublicKeyCredential: unknown }).PublicKeyCredential = function () {};
  });

  afterEach(() => {
    if (originalCredentials) {
      Object.defineProperty(navigator, "credentials", originalCredentials);
    } else {
      delete (navigator as unknown as { credentials?: unknown }).credentials;
    }
    (window as unknown as { PublicKeyCredential?: unknown }).PublicKeyCredential = originalPkc;
  });

  describe("base64url の相互変換", () => {
    it("パディングなしの base64url とバイナリを相互に変換できる", () => {
      // 0xfb 0xff は通常の base64 では "+/" を含む値になる
      const original = new Uint8Array([0xfb, 0xff, 0x00, 0x10]).buffer;

      const encoded = bufferToBase64Url(original);

      expect(encoded).toBe("-_8AEA");
      expect(bytes(base64UrlToBuffer(encoded))).toEqual([0xfb, 0xff, 0x00, 0x10]);
    });
  });

  describe("toCreationOptions / toRequestOptions", () => {
    it("登録オプションのチャレンジ・ユーザーID・除外クレデンシャルをバイナリへ変換する", () => {
      const actual = toCreationOptions(REGISTRATION_OPTIONS);

      expect(bytes(actual.challenge)).toEqual([4, 5, 6]);
      expect(bytes(actual.user.id)).toEqual([1, 2, 3]);
      expect(actual.user.name).toBe("testuser1");
      expect(bytes(actual.excludeCredentials![0].id)).toEqual([7, 8, 9]);
      expect(actual.authenticatorSelection?.userVerification).toBe("required");
      expect(actual.attestation).toBe("none");
    });

    it("認証オプションのチャレンジをバイナリへ変換し、RP ID・ユーザー検証をそのまま渡す", () => {
      const actual = toRequestOptions(AUTHENTICATION_OPTIONS);

      expect(bytes(actual.challenge)).toEqual([4, 5, 6]);
      expect(actual.rpId).toBe("localhost");
      expect(actual.userVerification).toBe("required");
      expect(actual.allowCredentials).toEqual([]);
    });
  });

  describe("credentialToJSON", () => {
    it("ブラウザが toJSON を実装していればその結果を使う", () => {
      const credential = { toJSON: () => ({ id: "from-browser" }) } as unknown as PublicKeyCredential;

      expect(JSON.parse(credentialToJSON(credential))).toEqual({ id: "from-browser" });
    });

    it("toJSON が無い場合、登録レスポンスを toJSON と同じ形式へ組み立てる", () => {
      const credential = {
        id: "AQI",
        rawId: new Uint8Array([1, 2]).buffer,
        type: "public-key",
        authenticatorAttachment: "platform",
        getClientExtensionResults: () => ({}),
        response: {
          clientDataJSON: new Uint8Array([3]).buffer,
          attestationObject: new Uint8Array([4]).buffer,
          getTransports: () => ["internal"],
        },
      } as unknown as PublicKeyCredential;

      expect(JSON.parse(credentialToJSON(credential))).toEqual({
        id: "AQI",
        rawId: "AQI",
        type: "public-key",
        authenticatorAttachment: "platform",
        clientExtensionResults: {},
        response: { clientDataJSON: "Aw", attestationObject: "BA", transports: ["internal"] },
      });
    });

    it("toJSON が無い場合、認証レスポンスを toJSON と同じ形式へ組み立てる", () => {
      const credential = {
        id: "AQI",
        rawId: new Uint8Array([1, 2]).buffer,
        type: "public-key",
        getClientExtensionResults: () => ({}),
        response: {
          clientDataJSON: new Uint8Array([3]).buffer,
          authenticatorData: new Uint8Array([5]).buffer,
          signature: new Uint8Array([6]).buffer,
          userHandle: new Uint8Array([7]).buffer,
        },
      } as unknown as PublicKeyCredential;

      expect(JSON.parse(credentialToJSON(credential)).response).toEqual({
        clientDataJSON: "Aw",
        authenticatorData: "BQ",
        signature: "Bg",
        userHandle: "Bw",
      });
    });
  });

  describe("isPasskeySupported", () => {
    it("PublicKeyCredential と navigator.credentials があれば true", () => {
      expect(isPasskeySupported()).toBe(true);
    });

    it("PublicKeyCredential が無いブラウザでは false", () => {
      delete (window as unknown as { PublicKeyCredential?: unknown }).PublicKeyCredential;

      expect(isPasskeySupported()).toBe(false);
    });
  });

  describe("createPasskeyCredential", () => {
    it("登録オプションを変換して navigator.credentials.create を呼び、JSON 文字列を返す", async () => {
      createMock.mockResolvedValueOnce({ toJSON: () => ({ id: "created" }) });

      const actual = await createPasskeyCredential(REGISTRATION_OPTIONS);

      expect(JSON.parse(actual)).toEqual({ id: "created" });
      const publicKey = createMock.mock.calls[0][0].publicKey;
      expect(bytes(publicKey.challenge)).toEqual([4, 5, 6]);
    });

    it("ユーザーがキャンセルした（NotAllowedError）場合は PasskeyCancelledError を投げる", async () => {
      createMock.mockRejectedValueOnce(domError("NotAllowedError"));

      await expect(createPasskeyCredential(REGISTRATION_OPTIONS)).rejects.toBeInstanceOf(
        PasskeyCancelledError
      );
    });

    it("登録済みの端末（InvalidStateError）の場合はその旨のメッセージを投げる", async () => {
      createMock.mockRejectedValueOnce(domError("InvalidStateError"));

      await expect(createPasskeyCredential(REGISTRATION_OPTIONS)).rejects.toThrow(
        "この端末のパスキーは既に登録されています"
      );
    });

    it("非対応のブラウザでは create を呼ばずにエラーを投げる", async () => {
      delete (window as unknown as { PublicKeyCredential?: unknown }).PublicKeyCredential;

      await expect(createPasskeyCredential(REGISTRATION_OPTIONS)).rejects.toThrow(
        "このブラウザはパスキーに対応していません"
      );
      expect(createMock).not.toHaveBeenCalled();
    });
  });

  describe("getPasskeyCredential", () => {
    it("認証オプションを変換して navigator.credentials.get を呼び、JSON 文字列を返す", async () => {
      getMock.mockResolvedValueOnce({ toJSON: () => ({ id: "asserted" }) });

      const actual = await getPasskeyCredential(AUTHENTICATION_OPTIONS);

      expect(JSON.parse(actual)).toEqual({ id: "asserted" });
      expect(getMock.mock.calls[0][0].mediation).toBeUndefined();
    });

    it("オートフィル（conditional）の場合は mediation と中止用のシグナルを渡す", async () => {
      getMock.mockResolvedValueOnce({ toJSON: () => ({ id: "asserted" }) });
      const controller = new AbortController();

      await getPasskeyCredential(AUTHENTICATION_OPTIONS, {
        conditional: true,
        signal: controller.signal,
      });

      expect(getMock.mock.calls[0][0].mediation).toBe("conditional");
      expect(getMock.mock.calls[0][0].signal).toBe(controller.signal);
    });

    it("中止（AbortError）された場合は PasskeyCancelledError を投げる", async () => {
      getMock.mockRejectedValueOnce(domError("AbortError"));

      await expect(getPasskeyCredential(AUTHENTICATION_OPTIONS)).rejects.toBeInstanceOf(
        PasskeyCancelledError
      );
    });

    it("想定外のエラーは既定のメッセージに変換する", async () => {
      getMock.mockRejectedValueOnce(domError("UnknownError"));

      await expect(getPasskeyCredential(AUTHENTICATION_OPTIONS)).rejects.toThrow(
        "パスキーによる認証に失敗しました"
      );
    });

    it("認証器が null を返した場合は PasskeyCancelledError を投げる", async () => {
      getMock.mockResolvedValueOnce(null);

      await expect(getPasskeyCredential(AUTHENTICATION_OPTIONS)).rejects.toBeInstanceOf(
        PasskeyCancelledError
      );
    });
  });
});
