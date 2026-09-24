import { render, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import ErrorPage from "../error";

describe("ErrorPage", () => {
  it("エラー内容をコンソールへ記録し、案内メッセージを表示すること", () => {
    const consoleErrorSpy = jest.spyOn(console, "error").mockImplementation(() => {});
    const error = new Error("something went wrong");
    const unstableRetry = jest.fn();

    render(<ErrorPage error={error} unstable_retry={unstableRetry} />);

    expect(consoleErrorSpy).toHaveBeenCalledWith(error);
    expect(
      screen.getByText(/予期しないエラーが発生しました。/)
    ).toBeInTheDocument();
    consoleErrorSpy.mockRestore();
  });

  it("再試行ボタンのクリックでunstable_retryが呼ばれること", () => {
    jest.spyOn(console, "error").mockImplementation(() => {});
    const unstableRetry = jest.fn();

    render(<ErrorPage error={new Error("boom")} unstable_retry={unstableRetry} />);
    fireEvent.click(screen.getByRole("button", { name: "再試行" }));

    expect(unstableRetry).toHaveBeenCalledTimes(1);
    jest.restoreAllMocks();
  });
});
