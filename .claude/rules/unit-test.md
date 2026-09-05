---
paths:
  - backend/src/test/**
  - "!backend/src/test/**/integration/**"
  - "!backend/src/test/java/com/web/gallery/WebGalleryApplicationTests.java"
  - "!backend/src/test/java/com/web/gallery/ArchitectureTest.java"
  - "!backend/src/test/java/com/web/gallery/ModulithDocumentationTest.java"
---

# ユニットテストのアーキテクチャルール

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ExtendWith(MockitoExtension.class)`でモック化した依存関係を使用

## 対象外

- `WebGalleryApplicationTests`はSpring Boot標準生成のアプリケーションコンテキストロード確認テストであり、本ルールの対象外とする
- `ArchitectureTest`はArchUnitによるクラスパス解析ベースのアーキテクチャ検証テストであり、Mockitoによるモック化を前提としないため本ルールの対象外とする
- `ModulithDocumentationTest`はSpring Modulithによるドキュメント生成テストであり、Mockitoによるモック化を前提としないため本ルールの対象外とする
