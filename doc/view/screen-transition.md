# 📺 画面遷移図

## 画面一覧

| # | 画面名 | ビュー名 | URLパス | アクセス制御 |
|---|--------|----------|---------|-------------|
| 1 | ログイン | `login` | `/login` | 公開 |
| 2 | アカウント登録 | `account_register` | `/register` | 公開 |
| 3 | アカウント一覧 | `account_list` | `/account_list` | 公開 |
| 4 | アカウント設定 | `account_setting` | `/{accountId}/account_setting` | 認証必須（本人のみ） |
| 5 | 写真一覧 | `photo_list` | `/photo/{photoAccountId}/photo_list` | 公開 |
| 6 | 写真詳細 | `photo_detail` | `/photo/{photoAccountId}/photo_detail` | 公開 |
| 7 | 写真設定 | `photo_setting` | `/photo/{photoAccountId}/photo_setting` | 認証必須（本人のみ） |

---

## 全体遷移図

```mermaid
graph TD
    subgraph 認証フロー
        ROOT["/ （ルート）"] -->|未認証| LOGIN["ログイン\n/login"]
        ROOT -->|認証済み| PHOTO_LIST
        LOGIN -->|ログイン成功| PHOTO_LIST
        LOGIN -->|ログイン失敗| LOGIN
        LOGIN -->|アカウント作成リンク| REGISTER["アカウント登録\n/register"]
        REGISTER -->|登録成功| LOGIN
        REGISTER -->|← back| LOGIN
    end

    subgraph メイン画面
        ACCOUNT_LIST["アカウント一覧<br/>/account_list"]
        PHOTO_LIST["写真一覧<br/>/photo/{id}/photo_list"]
        PHOTO_DETAIL["写真詳細<br/>/photo/{id}/photo_detail"]
        PHOTO_SETTING["写真設定<br/>/photo/{id}/photo_setting"]
        ACCOUNT_SETTING["アカウント設定<br/>/{id}/account_setting"]
    end

    ACCOUNT_LIST -->|ギャラリーボタン| PHOTO_LIST
    PHOTO_LIST -->|写真選択| PHOTO_DETAIL
    PHOTO_LIST -->|＋写真追加| PHOTO_SETTING
    PHOTO_DETAIL -->|← back| PHOTO_LIST
    PHOTO_DETAIL -->|編集アイコン| PHOTO_SETTING
    PHOTO_DETAIL -->|削除成功| PHOTO_LIST
    PHOTO_SETTING -->|登録/更新成功| PHOTO_LIST
    PHOTO_SETTING -->|← back| PHOTO_LIST
    ACCOUNT_SETTING -->|← back| PHOTO_LIST
    ACCOUNT_SETTING -->|ID/PW変更| LOGIN

    REGISTER -->|登録失敗| REGISTER
    ACCOUNT_SETTING -->|更新失敗| ACCOUNT_SETTING
    PHOTO_DETAIL -->|削除失敗| PHOTO_DETAIL
    PHOTO_SETTING -->|登録/更新失敗| PHOTO_SETTING
```

---

## 共通メニュー遷移

全画面のハンバーガーメニューから以下の遷移が可能です。

```mermaid
graph LR
    MENU["ハンバーガーメニュー"]

    MENU -->|"Sign In<br/>（未認証時）"| LOGIN["ログイン<br/>/login"]
    MENU -->|"Photographers"| ACCOUNT_LIST["アカウント一覧<br/>/account_list"]
    MENU -->|"My Gallary<br/>（認証済み時）"| PHOTO_LIST["写真一覧<br/>/photo/{id}/photo_list"]
    MENU -->|"Account Setting<br/>（認証済み時）"| ACCOUNT_SETTING["アカウント設定<br/>/{id}/account_setting"]
    MENU -->|"Sign Out<br/>（認証済み時）"| LOGOUT["ログアウト → /login"]
```

---

## 認証フロー詳細

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant Login as ログイン画面
    participant Register as アカウント登録画面
    participant API as REST API
    participant PhotoList as 写真一覧画面

    Note over User, PhotoList: アカウント登録フロー
    User->>Login: /login にアクセス
    User->>Login: アカウント作成リンクをクリック
    Login->>Register: /register に遷移
    User->>Register: フォーム入力・送信
    Register->>API: POST /api/v1/accounts
    API-->>Register: 登録成功
    Register->>Register: モーダル表示
    Register->>Login: /login に遷移

    Note over User, PhotoList: ログインフロー
    User->>Login: 認証情報を入力・送信
    Login->>API: POST /api/v1/auth/login
    API-->>Login: JWT発行（アクセストークン + リフレッシュトークン）
    Login->>PhotoList: /photo/{id}/photo_list にリダイレクト
```

---

## 写真管理フロー詳細

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant List as 写真一覧画面
    participant Detail as 写真詳細画面
    participant Setting as 写真設定画面
    participant API as REST API

    Note over User, API: 写真閲覧フロー
    User->>List: /photo/{id}/photo_list にアクセス
    List->>API: GET /api/v1/accounts/{id}/photos
    API-->>List: 写真データ返却
    User->>List: もっと見るボタン
    List->>API: GET（ページ追加読み込み）
    User->>List: 写真サムネイル選択
    List->>Detail: /photo/{id}/photo_detail に遷移

    Note over User, API: 写真登録フロー
    User->>List: ＋写真追加ボタン
    List->>Setting: /photo/{id}/photo_setting に遷移
    User->>Setting: 画像アップロード・メタデータ入力・送信
    Setting->>API: POST /api/v1/accounts/{id}/photos
    API-->>Setting: 登録成功
    Setting->>Setting: モーダル表示
    Setting->>List: /photo/{id}/photo_list に遷移

    Note over User, API: 写真編集フロー
    User->>Detail: 編集アイコンをクリック
    Detail->>Setting: /photo/{id}/photo_setting に遷移
    User->>Setting: メタデータ編集・送信
    Setting->>API: PUT /api/v1/accounts/{id}/photos
    API-->>Setting: 更新成功
    Setting->>List: /photo/{id}/photo_list に遷移

    Note over User, API: 写真削除フロー
    User->>Detail: 削除アイコンをクリック
    Detail->>API: DELETE /api/v1/accounts/{id}/photos
    API-->>Detail: 削除成功
    Detail->>Detail: モーダル表示
    Detail->>List: /photo/{id}/photo_list に遷移
```

---

## アカウント設定フロー詳細

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant Setting as アカウント設定画面
    participant API as REST API
    participant Login as ログイン画面

    User->>Setting: /{id}/account_setting にアクセス
    User->>Setting: プロフィール情報を編集・送信

    alt 通常の更新
        Setting->>API: PUT /api/v1/accounts/{id}
        API-->>Setting: 更新成功
        Setting->>Setting: モーダル表示（5秒で自動クローズ）
    else アカウントID/パスワード変更
        Setting->>API: PUT /api/v1/accounts/{id}
        API-->>Setting: 更新成功
        Setting->>Setting: アラート表示
        Setting->>Login: ログアウト → /login に遷移
    else 更新失敗（アカウントID重複）
        Setting->>API: PUT /api/v1/accounts/{id}
        API-->>Setting: エラー返却
        Setting->>Setting: エラーメッセージ表示
    end
```

---

## お気に入りフロー

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant Detail as 写真詳細画面
    participant API as REST API

    User->>Detail: お気に入りアイコンをクリック

    alt お気に入り追加
        Detail->>API: POST /api/v1/photos/favorites
        API-->>Detail: 成功
        Detail->>Detail: アイコン状態を切替
    else お気に入り解除
        Detail->>API: DELETE /api/v1/photos/favorites
        API-->>Detail: 成功
        Detail->>Detail: アイコン状態を切替
    end
```

---

## 遷移詳細テーブル

### ルート (`/`)

| 条件 | 遷移先 |
|------|--------|
| 未認証 | `/login` にリダイレクト |
| 認証済み | `/photo/{accountId}/photo_list` にリダイレクト |

### ログイン (`/login`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| ログイン成功 | `/photo/{accountId}/photo_list` | AJAX（POST /api/v1/auth/login） |
| ログイン失敗 | `/login`（エラー表示） | 画面内表示 |
| 「アカウント作成」リンク | `/register` | リンク |

### アカウント登録 (`/register`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 登録成功 | `/login` | AJAX → モーダル → フォーム送信 |
| 登録失敗（重複） | `/register`（エラー表示） | 画面内表示 |
| 登録失敗（その他） | 同画面（エラー表示） | 画面内表示 |
| 「← back」リンク | `/login` | リンク |

### アカウント一覧 (`/account_list`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 「ギャラリー」ボタン | `/photo/{accountId}/photo_list` | リンク |
| メニュー「Sign In」 | `/login` | リンク（未認証時） |
| メニュー「My Gallary」 | `/photo/{accountId}/photo_list` | リンク（認証済み時） |
| メニュー「Sign Out」 | `/login` | ログアウト |

### アカウント設定 (`/{accountId}/account_setting`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 更新成功 | 同画面（モーダル表示） | AJAX |
| アカウントID/パスワード変更 | `/login` | AJAX → アラート → ログアウト |
| 更新失敗（重複） | 同画面（エラー表示） | 画面内表示 |
| 更新失敗（その他） | 同画面（エラー表示） | 画面内表示 |
| 「← back」リンク | `/photo/{accountId}/photo_list` | リンク |
| メニュー「Sign Out」 | `/login` | ログアウト |

### 写真一覧 (`/photo/{photoAccountId}/photo_list`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 写真サムネイル選択 | `/photo/{photoAccountId}/photo_detail` | リンク |
| 「＋写真追加」ボタン | `/photo/{photoAccountId}/photo_setting` | リンク（本人のみ） |
| 「もっと見る」ボタン | 同画面（追加読み込み） | AJAX |
| 絞り込みフィルター | 同画面（再読み込み） | AJAX |
| メニュー「Photographers」 | `/account_list` | リンク |
| メニュー「My Gallary」 | `/photo/{accountId}/photo_list` | リンク |
| メニュー「Account Setting」 | `/{accountId}/account_setting` | リンク |
| メニュー「Sign Out」 | `/login` | ログアウト |

### 写真詳細 (`/photo/{photoAccountId}/photo_detail`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 編集アイコン | `/photo/{photoAccountId}/photo_setting` | リンク（本人のみ） |
| 削除アイコン → 成功 | `/photo/{photoAccountId}/photo_list` | AJAX → モーダル → リダイレクト |
| 削除アイコン → 失敗 | 同画面（エラー表示） | 画面内表示 |
| お気に入りアイコン | 同画面（状態切替） | AJAX（認証済み時のみ） |
| 「← back」リンク | `/photo/{photoAccountId}/photo_list` | リンク |
| メニュー「Photographers」 | `/account_list` | リンク |
| メニュー「My Gallary」 | `/photo/{accountId}/photo_list` | リンク |
| メニュー「Account Setting」 | `/{accountId}/account_setting` | リンク |
| メニュー「Sign Out」 | `/login` | ログアウト |

### 写真設定 (`/photo/{photoAccountId}/photo_setting`)

| 操作 | 遷移先 | 方式 |
|------|--------|------|
| 登録/更新成功 | `/photo/{photoAccountId}/photo_list` | AJAX → モーダル → フォーム送信 |
| 登録/更新失敗 | 同画面（エラー表示） | 画面内表示 |
| 「← back」リンク | `/photo/{photoAccountId}/photo_list` | リンク |
| メニュー「Sign Out」 | `/login` | ログアウト |

---

## REST API（画面遷移に関連するもの）

画面からAJAXで呼び出されるAPIの一覧です。結果に応じて画面遷移が発生します。

| API | メソッド | 呼び出し元画面 | 成功時の遷移 |
|-----|---------|-------------|------------|
| `/api/v1/auth/login` | POST | ログイン | → 写真一覧 |
| `/api/v1/auth/logout` | POST | 共通メニュー | → ログイン |
| `/api/v1/accounts` | POST | アカウント登録 | → ログイン |
| `/api/v1/accounts/{accountId}` | PUT | アカウント設定 | → 同画面 or ログイン |
| `/api/v1/accounts/{photoAccountId}/photos` | GET | 写真一覧 | なし（データ表示） |
| `/api/v1/accounts/{photoAccountId}/photos` | POST | 写真設定（新規） | → 写真一覧 |
| `/api/v1/accounts/{photoAccountId}/photos` | PUT | 写真設定（編集） | → 写真一覧 |
| `/api/v1/accounts/{photoAccountId}/photos` | DELETE | 写真詳細 | → 写真一覧 |
| `/api/v1/photos/favorites` | POST | 写真詳細 | なし（状態切替） |
| `/api/v1/photos/favorites` | DELETE | 写真詳細 | なし（状態切替） |
