---
paths:
  - backend/**/model/**
---

# Modelクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Value` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Data`、`@Getter`、`@Setter`

## @NonNullアノテーション

- Null許容しないプロパティには`@NonNull`アノテーションを付与すること
- `@NonNull`が一つも使われていないModelクラスは違反の可能性がある

## 命名規則

- クラス名サフィックス: `Model`
- Modelのコレクションを表すクラスのクラス名サフィックス: `ModelList`（例：`AccountModelList`、`PhotoModelList`）

## コレクションクラス（`XxxModelList`）

- 対象のModelクラスのリストを1フィールドで保持し、`@NonNull`を付与する
- ソート機能・フィルター機能はインスタンスメソッドとして提供し、新しい`XxxModelList`を返すこと（元のインスタンスを変更しない）
- ファクトリメソッドとして、Modelのリストから生成する`of()`、対応するEntity等のリストから生成する`from()`、空インスタンスを生成する`empty()`を提供すること
- `size()`、`isEmpty()`、`get(int)`、`stream()`、`toList()`を提供し、`Iterable<XxxModel>`を実装すること
