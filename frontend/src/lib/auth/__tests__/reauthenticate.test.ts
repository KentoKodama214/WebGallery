import { reauthenticateWithPasskey } from "../reauthenticate";

const mockGetReauthenticationOptions = jest.fn();
const mockGetPasskeyCredential = jest.fn();

jest.mock("@/lib/api/client", () => ({
  getReauthenticationOptions: (...args: unknown[]) => mockGetReauthenticationOptions(...args),
}));

jest.mock("@/lib/auth/passkey", () => ({
  getPasskeyCredential: (...args: unknown[]) => mockGetPasskeyCredential(...args),
}));

describe("reauthenticateWithPasskey", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("本人確認オプションを発行し、そのチャレンジに端末のパスキーで署名した認証レスポンスを返す", async () => {
    const options = { challenge: "c", allowCredentials: [{ id: "a", type: "public-key" }] };
    mockGetReauthenticationOptions.mockResolvedValueOnce(options);
    mockGetPasskeyCredential.mockResolvedValueOnce('{"id":"a"}');

    await expect(reauthenticateWithPasskey("testuser1")).resolves.toBe('{"id":"a"}');
    expect(mockGetReauthenticationOptions).toHaveBeenCalledWith("testuser1");
    expect(mockGetPasskeyCredential).toHaveBeenCalledWith(options);
  });

  it("本人確認オプションの発行に失敗した場合は、パスキーを要求せず例外をそのまま伝える", async () => {
    mockGetReauthenticationOptions.mockRejectedValueOnce(new Error("本人確認を開始できませんでした"));

    await expect(reauthenticateWithPasskey("testuser1")).rejects.toThrow(
      "本人確認を開始できませんでした"
    );
    expect(mockGetPasskeyCredential).not.toHaveBeenCalled();
  });
});
