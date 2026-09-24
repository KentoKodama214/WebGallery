---
paths:
  - backend/src/test/**/type_handler/**
  - "!backend/src/test/**/integration/**"
---

# TypeHandlerパッケージの単体テスト規約

## 命名規則

- クラス名に`Test`サフィックスを付与すること

## テスト種別

- `@ActiveProfiles("test")`をテストクラスに付与すること
- `@ExtendWith(MockitoExtension.class)`を付与し、`@InjectMocks`でTypeHandler本体を、`@Mock`で`PreparedStatement`・`ResultSet`・`CallableStatement`の3つを用意すること

## テストの構造

- `@Nested`クラスは`MyBatis`の`BaseTypeHandler`の4メソッドに対応する以下の4つに固定すること
  - `setNonNullParameter`
  - `getNullableResultByColumnName`
  - `getNullableResultByColumnIndex`
  - `getNullableResultByCallableStatement`
- `@Nested`クラスには`@Order`と`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`を付与し、クラス内の`@Test`メソッドにも`@Order`を付与して実行順を明示する

## 各メソッドの検証観点

- `setNonNullParameter`は、`PreparedStatement`に対して意図した値・型で`set`メソッドが呼ばれたことを`verify(preparedStatement).setXxx(...)`で検証すること
- `getNullableResultByColumnName`は`ResultSet#getString(String)`等をスタブし、値が一致するケース・DB値が`null`のケース・どの値にも一致しないケースの3パターンを検証すること。対象Enumの選択肢数が少ない場合は全値を網羅すること
- `getNullableResultByColumnIndex`と`getNullableResultByCallableStatement`は、`getNullableResultByColumnName`へ処理を委譲する実装であるため、代表的な1ケースのみでよい

## `@DisplayName`

- 原則、「正常系：」または「異常系：」で始まる日本語の説明文とする
