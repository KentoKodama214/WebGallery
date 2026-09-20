import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import PhotoListPage from "../page";

jest.mock("@/components/layout/Header", () => ({
  Header: () => <div data-testid="header" />,
}));
jest.mock("@/components/layout/Footer", () => ({
  Footer: () => <div data-testid="footer" />,
}));
jest.mock("../PhotoList", () => ({
  PhotoList: ({ photoAccountId }: { photoAccountId: string }) => (
    <div data-testid="photo-list">{photoAccountId}</div>
  ),
}));

describe("PhotoListPage", () => {
  it("photoAccountIdの形式が正しい場合はPhotoListを描画する", async () => {
    const page = await PhotoListPage({
      params: Promise.resolve({ photoAccountId: "e2etestaccount1" }),
    });
    render(page);

    expect(screen.getByTestId("photo-list")).toHaveTextContent(
      "e2etestaccount1"
    );
  });

  it.each([
    ["短すぎる", "a"],
    ["記号を含む", "invalid-account!"],
    ["パストラバーサル", "../evil"],
  ])("photoAccountIdが%s場合はギャラリーが見つからない旨を表示する", async (_label, value) => {
    const page = await PhotoListPage({
      params: Promise.resolve({ photoAccountId: value }),
    });
    render(page);

    expect(screen.getByText("ギャラリーが見つかりません")).toBeInTheDocument();
    expect(screen.queryByTestId("photo-list")).not.toBeInTheDocument();
  });
});
