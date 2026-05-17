"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/AuthProvider";
import styles from "./Header.module.css";

/**
 * ヘッダーコンポーネント
 * ハンバーガーメニューで認証状態に応じたナビゲーションを表示
 */
export function Header() {
  const { isAuthenticated, user, logout } = useAuth();
  const router = useRouter();
  const [isOpen, setIsOpen] = useState(false);

  const toggleMenu = () => {
    setIsOpen(!isOpen);
  };

  const closeMenu = () => {
    setIsOpen(false);
  };

  const handleLogout = async () => {
    closeMenu();
    await logout();
    router.push("/login");
  };

  return (
    <header>
      {/* ハンバーガーボタン */}
      <div
        className={`${styles.buttonContainer} ${isOpen ? styles.active : ""}`}
        onClick={toggleMenu}
        data-testid="hamburger-button"
      >
        <span style={{ top: 0 }}></span>
        <span style={{ top: 10 }}></span>
        <span style={{ top: 20 }}></span>
      </div>

      {/* オーバーレイメニュー */}
      <div
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
