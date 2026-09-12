---
paths:
  - backend/**/model/**
---

# Modelクラスのアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Model`
- Modelのコレクションを表すクラスのクラス名サフィックス: `ModelList`（例：`AccountModelList`、`PhotoModelList`）
- 例外：`domain/`配下の値オブジェクトのコレクションを表すクラスは、`Model`を含まない`XxxList`とする（例：`PhotoNoList`）

## Lombokアノテーション規約

- **許可**: `@Value` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Data`、`@Getter`、`@Setter`

## @NonNullアノテーション

- Null許容しないプロパティには`@NonNull`アノテーションを付与すること
- `@NonNull`が一つも使われていないModelクラスは違反の可能性がある
  - 例外：`AccountModel`は部分更新用の複数のファクトリメソッド（`forUnlock`等）を持ち、全ファクトリメソッドに共通して必須となるプロパティが存在しないため、意図的に`@NonNull`を使用しない

## コレクションオブジェクト（ファーストクラスコレクション、`XxxModelList`）

- `record XxxModelList(List<XxxModel> xxxModelList) implements Iterable<XxxModel>`として実装する（Lombokの`@Value`/`@Builder`は使用しない）
- コンパクトコンストラクタで`Objects.requireNonNull()`によるnullチェックを行う
- クラスのJavadocコメントに、レコードコンポーネントを説明する`@param`タグを付与すること（checkstyleのJavadocTypeルールで必須）
- ソート機能・フィルター機能はインスタンスメソッドとして提供し、新しい`XxxModelList`を返すこと（元のインスタンスを変更しない）
- ファクトリメソッドとして、Modelのリストから生成する`of()`、対応するEntity等のリストから生成する`from()`、空インスタンスを生成する`empty()`を提供すること
- `size()`、`isEmpty()`、`get(int)`、`stream()`、`toList()`を提供し、`iterator()`をオーバーライドすること

## 検証

`@NonNull`の付与、および`XxxModelList`がrecordかつ`Iterable`を実装していることは`backend/src/test/java/com/web/gallery/architecture/ModelArchitectureTest.java`のArchUnitテストで機械的に検証される。ただし`@Value`/`@Builder`のLombokアノテーション規約は、コンパイラが`RetentionPolicy.SOURCE`で完全に除去しバイトコードに一切残らないため、バイトコード解析であるArchUnitでは原理的に検証不可能であり、`backend-architecture-checker`によるレビューで担保する。
