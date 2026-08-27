---
paths:
  - backend/**/domain/**
---

# Domainクラスのアーキテクチャルール

- `record Xxx(型 xxx) implements Serializable`として実装する
- コンパクトコンストラクタで値のチェックや初期値の代入を行う
- 例外：`ImageFile`は`MultipartFile`をラップしており、`MultipartFile`自体が`Serializable`を実装しないため`Serializable`を実装しない

## 複合値オブジェクト

- 関連する複数のプロパティをグループ化する場合は、`record Xxx(型1 xxx1, 型2 xxx2, ...) implements Serializable`として複数プロパティを持つrecordを許容する（例：`ExifData`、`GeoLocation`）
- 各プロパティは個別にnull許容とし、複合値オブジェクト自体のコンパクトコンストラクタでのnullチェックは行わない
- 全プロパティが未設定の複合値オブジェクトを生成する`empty()`静的ファクトリメソッドを提供すること
