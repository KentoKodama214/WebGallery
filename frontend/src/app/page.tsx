import { redirect } from "next/navigation";

/**
 * ルートページ
 * ログインページへリダイレクトする（認証後のリダイレクトはクライアント側で処理）
 */
export default function Home() {
  redirect("/login");
}
