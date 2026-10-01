"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import localFont from "next/font/local";
import { useAuth } from "@/lib/auth/AuthProvider";
import { markBackgroundInert, onActivateKey } from "@/lib/a11y";
import styles from "./Header.module.css";

/**
 * ナビゲーション見出しのフォント（Vollkorn 400・latin サブセット）
 *
 * フォントファイルはリポジトリに同梱してセルフホストする。Google Fonts への
 * ランタイム／ビルド時リクエスト（可用性・プライバシー・オフラインビルド上の
 * 懸念）を一切発生させない。ナビの表記は英字のみのため latin サブセットで足りる。
 */
const navFont = localFont({
  src: "./fonts/vollkorn-latin-400.woff2",
  weight: "400",
  display: "swap",
  fallback: ["serif"],
});

/**
 * ヘッダーコンポーネント
 * ハンバーガーメニューで認証状態に応じたナビゲーションを表示
 */
export function Header() {
  const { isAuthenticated, user, logout } = useAuth();
  const router = useRouter();
  const [isOpen, setIsOpen] = useState(false);
  const buttonRef = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const headerRef = useRef<HTMLElement>(null);

  const toggleMenu = () => {
    setIsOpen((prev) => !prev);
  };

  const closeMenu = () => {
    setIsOpen(false);
  };

  // メニュー展開中は Escape で閉じ、Tab フォーカスをメニュー内で循環させ、
  // 背面を `inert` にして支援技術のブラウズモードから隔離する
  // （挙動はモーダルダイアログと同じなので `ModalDialog` / 写真一覧のフィルターパネルと揃える）。
  // 閉じたときはトグルボタンへフォーカスを戻す。
  useEffect(() => {
    if (!isOpen) return;

    const menu = menuRef.current;
    // トグルボタンは Header がマウントされている間は同一 DOM ノードのため、
    // effect 実行時に控えてクリーンアップ（＝メニューを閉じた時）のフォーカス復帰に使う
    const toggleButton = buttonRef.current;

    // 背面コンテンツを inert にする。メニューは条件付きレンダリングではなく常にDOM上に
    // あるため、開いている間だけ付与する。
    //
    // 基準にするのはメニューではなく `<header>` 全体。メニューを基準にすると、その兄弟である
    // ハンバーガーボタン（展開中は「閉じる」操作を担う）まで inert になり、閉じられなくなる
    const restoreInert = headerRef.current
      ? markBackgroundInert(headerRef.current)
      : () => {};

    const focusables = menu
      ? Array.from(
          menu.querySelectorAll<HTMLElement>('a[href], button:not([disabled])')
        )
      : [];
    focusables[0]?.focus();

    const handleKeyDown = (e: globalThis.KeyboardEvent) => {
      if (e.key === "Escape") {
        e.preventDefault();
        setIsOpen(false);
        return;
      }
      if (e.key !== "Tab" || focusables.length === 0) return;
      const first = focusables[0];
      const last = focusables[focusables.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", handleKeyDown, true);
    return () => {
      document.removeEventListener("keydown", handleKeyDown, true);
      restoreInert();
      toggleButton?.focus();
    };
  }, [isOpen]);

  const handleLogout = async () => {
    closeMenu();
    await logout();
    router.push("/login");
  };

  return (
    <header ref={headerRef}>
      {/* ハンバーガーボタン */}
      <div
        ref={buttonRef}
        className={`${styles.buttonContainer} ${isOpen ? styles.active : ""}`}
        onClick={toggleMenu}
        onKeyDown={onActivateKey(toggleMenu)}
        role="button"
        tabIndex={0}
        aria-label="メニュー"
        aria-expanded={isOpen}
        aria-controls="header-overlay-menu"
        data-testid="hamburger-button"
      >
        <span style={{ top: 0 }}></span>
        <span style={{ top: 10 }}></span>
        <span style={{ top: 20 }}></span>
      </div>

      {/* オーバーレイメニュー。
          展開中は画面全体を覆いEscape・Tab循環・フォーカス復帰・背面のinert化を行うため、
          挙動はモーダルダイアログと同じ。メニューは条件付きレンダリングではなく常にDOM上に
          あるため、これらの属性は展開中だけ付ける
          （`aria-modal="true"` を閉じている間も残すと、背面コンテンツを隠したまま扱う支援技術がある） */}
      <div
        ref={menuRef}
        id="header-overlay-menu"
        role={isOpen ? "dialog" : undefined}
        aria-modal={isOpen ? true : undefined}
        aria-label={isOpen ? "メニュー" : undefined}
        className={`${styles.overlay} ${isOpen ? styles.open : ""}`}
        data-testid="overlay-menu"
      >
        <nav className={`${styles.nav} ${navFont.className}`}>
          {isAuthenticated && user ? (
            <ul className={styles.menu}>
              <li className={styles.menuItem}>
                <Link
                  href={`/photo/${user.accountId}/photo_list`}
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  My Gallery
                </Link>
              </li>
              <li className={styles.menuItem}>
                <Link
                  href="/account_list"
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  Photographers
                </Link>
              </li>
              <li className={styles.menuItem}>
                <Link
                  href={`/${user.accountId}/account_setting`}
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  Account Setting
                </Link>
              </li>
              <li className={styles.menuItem}>
                <Link
                  href="/inquiry/list"
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  Inquiry
                </Link>
              </li>
              <li className={styles.menuItem}>
                <button
                  onClick={handleLogout}
                  className={styles.menuLink}
                  data-testid="logout-button"
                >
                  Sign Out
                </button>
              </li>
            </ul>
          ) : (
            <ul className={styles.menu}>
              <li className={styles.menuItem}>
                <Link
                  href="/account_list"
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  Photographers
                </Link>
              </li>
              <li className={styles.menuItem}>
                <Link
                  href="/login"
                  className={styles.menuLink}
                  onClick={closeMenu}
                >
                  Sign In
                </Link>
              </li>
            </ul>
          )}
        </nav>
      </div>
    </header>
  );
}
