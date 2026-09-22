interface FooterProps {
  /**
   * 背景の明るさに応じた文字色。明るい背景（whitesmoke等）のページでは
   * "light" を指定する。省略時は暗い背景（黒・濃紺）向けの既定色を使う
   */
  variant?: "light" | "dark";
}

/**
 * フッターコンポーネント
 * 画面右下に固定表示されるコピーライト表示
 */
export function Footer({ variant = "dark" }: FooterProps) {
  const textColorClass = variant === "light" ? "text-gray-600" : "text-gray-400";

  return (
    <div
      className={`fixed bottom-0 right-[5px] h-[30px] w-[200px] text-right text-xs ${textColorClass} z-[1]`}
    >
      <p>&copy; {new Date().getFullYear()} KENTO KODAMA</p>
    </div>
  );
}
