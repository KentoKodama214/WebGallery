# セキュリティ

## 認証方式

Spring SecurityによるJWT（JSON Web Token）認証を採用しています。ステートレスなセッション管理を行い、アクセストークンとリフレッシュトークンによる認証フローを実装しています。

## セキュリティ設定

| 項目 | 設定内容 |
|------|----------|
| 認証方式 | JWT Bearer Token（ステートレス） |
| パスワードハッシュ | BCrypt |
| アクセストークン有効期限 | 15分 |
| リフレッシュトークン有効期限 | 7日 |
| アカウントロック | ログイン失敗3回でロック |

## 認証フロー

1. ログイン時に `POST /api/v1/auth/login` でアクセストークン（レスポンスボディ）とリフレッシュトークン（HttpOnly cookie）を発行
2. APIリクエストには `Authorization: Bearer {accessToken}` ヘッダーを付与
3. アクセストークン期限切れ時は `POST /api/v1/auth/refresh` でトークンを再発行
4. ログアウト時は `POST /api/v1/auth/logout` でリフレッシュトークンを無効化

## アクセス制御

| リソース | アクセス制御 |
|----------|--------------|
| 認証API（`/api/v1/auth/**`） | 公開 |
| アカウント登録（`POST /api/v1/accounts`） | 公開 |
| アカウント一覧（`GET /api/v1/accounts`） | 公開 |
| 写真の閲覧（`GET /api/v1/accounts/{id}/photos/**`） | 公開 |
| 都道府県一覧（`GET /api/v1/prefectures`） | 公開 |
| 写真の登録・編集・削除 | 認証必須（本人のみ） |
| お気に入り登録・解除 | 認証必須 |
| アカウント詳細取得・更新 | 認証必須（本人のみ） |

## フロントエンド（API プロキシ）側の防御

フロントエンド（`frontend/`）は既定で同一オリジンの `/api/*` プロキシ（`src/app/api/[...path]/route.ts`）
経由でバックエンドと通信する。プロキシは以下を担う。

- 状態変更メソッド（POST/PUT/DELETE/PATCH）に対し、`Sec-Fetch-Site: cross-site` の拒否と
  `Origin` の自サイト一致検証（CSRF 多層防御。バックエンドの SameSite Cookie と併用）
- パストラバーサル（`.` / `..` / 空セグメント）の拒否、リクエストボディの上限（6MB）、
  バックエンドへの中継タイムアウト（30 秒）、リダイレクト追従の無効化（`redirect: "manual"`）
- クライアント由来の転送系ヘッダー（`X-Forwarded-*` 等）の除去、`Location` の正規化
  （バックエンド絶対 URL は相対化、外部 URL・プロトコル相対は削除）

> **注意**: `NEXT_PUBLIC_API_BASE_URL` を設定して別オリジンのバックエンドを直接叩く構成にした場合、
> 上記プロキシの CSRF 検証はバイパスされる。その構成ではバックエンド側の CSRF 対策に完全に依存する。

しきい値等の定数（例: アカウントロックのログイン失敗回数 = 3）は `frontend/src/lib/consts.ts` で
バックエンドの `application.yml` と一致させて管理している。
