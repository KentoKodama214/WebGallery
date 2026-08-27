---
paths:
  - backend/src/test/**
  - "!backend/src/test/**/integration/**"
  - "!backend/src/test/java/com/web/gallery/WebGalleryApplicationTests.java"
---

# ユニットテストのアーキテクチャルール

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用

## 対象外

- `WebGalleryApplicationTests`はSpring Boot標準生成のアプリケーションコンテキストロード確認テストであり、本ルールの対象外とする
