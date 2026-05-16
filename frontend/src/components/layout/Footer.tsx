/**
 * フッターコンポーネント
 * 画面右下に固定表示されるコピーライト表示
 */
export function Footer() {
  return (
    <div className="fixed bottom-0 right-[5px] h-[30px] w-[200px] text-right text-xs text-gray-400 z-[1]">
      <p>&copy; {new Date().getFullYear()} KENTO KODAMA</p>
    </div>
  );
}
