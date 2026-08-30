"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import { onActivateKey } from "@/lib/a11y";
import styles from "./Header.module.css";

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

  const toggleMenu = () => {
    setIsOpen((prev) => !prev);
  };

  const closeMenu = () => {
    setIsOpen(false);
  };

  // メニュー展開中は Escape で閉じ、Tab フォーカスをメニュー内で循環させる。
  // 閉じたときはトグルボタンへフォーカスを戻す。
  useEffect(() => {
    if (!isOpen) return;

    const menu = menuRef.current;
    // トグルボタンは Header がマウントされている間は同一 DOM ノードのため、
    // effect 実行時に控えてクリーンアップ（＝メニューを閉じた時）のフォーカス復帰に使う
    const toggleButton = buttonRef.current;
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
      toggleButton?.focus();
    };
  }, [isOpen]);

  const handleLogout = async () => {
    closeMenu();
    await logout();
    router.push("/login");
  };

  return (
    <header>
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

      {/* オーバーレイメニュー */}
      <div
        ref={menuRef}
        id="header-overlay-menu"
        className={`${styles.overlay} ${isOpen ? styles.open : ""}`}
        data-testid="overlay-menu"
      >
        <nav className={styles.nav}>
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
