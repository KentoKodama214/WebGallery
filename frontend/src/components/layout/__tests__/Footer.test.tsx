import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { Footer } from "../Footer";

describe("Footer", () => {
  it("現在の年を含むコピーライト表示がされること", () => {
    render(<Footer />);

    const currentYear = new Date().getFullYear();
    expect(
      screen.getByText(`© ${currentYear} KENTO KODAMA`)
    ).toBeInTheDocument();
  });
});
