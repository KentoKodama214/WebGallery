# レイヤード・アーキテクチャ

## 全体構成図

```mermaid
graph TB
    Client["クライアント<br>（Next.js フロントエンド）"]

    subgraph Controller["Controller層"]
        REST["RESTコントローラ<br>JSON API"]
    end

    subgraph Service["Service層"]
        SI["Service<br>インターフェース"]
        SImpl["ServiceImpl<br>ビジネスロジック・バリデーション"]
        SI --> SImpl
    end

    subgraph Repository["Repository層"]
        RI["Repository<br>インターフェース"]
        RImpl["RepositoryImpl<br>データアクセスの抽象化"]
        RI --> RImpl
    end

    subgraph Mapper["Mapper層"]
        MI["Mapperインターフェース"]
        MX["Mapper XML<br>SQL定義"]
        MI --> MX
    end

    DB[("PostgreSQL<br>commonスキーマ<br>photoスキーマ")]

    Client -- "APIリクエスト" --> REST
    REST -- "Request/Response DTO" --> SI
    SImpl -- "Modelオブジェクト" --> RI
    RImpl --> MI
    MX --> DB
```

## 各レイヤーの役割

| レイヤー | 役割 | 主なパッケージ |
|----------|------|----------------|
| Controller | RESTコントローラ（JSON API） | `controller/` |
| Service | ビジネスロジック・バリデーション | `service/`, `service/impl/` |
| Repository | データアクセスの抽象化 | `repository/`, `repository/impl/` |
| Mapper | MyBatisによるSQL実行 | `mapper/`, `resources/com/web/gallery/mapper/` |

## 設計方針

### インターフェースと実装の分離

Service・Repositoryはインターフェースと実装クラスに分離しています。

```
service/
├── PhotoService.java          # インターフェース
└── impl/
    └── PhotoServiceImpl.java  # 実装クラス（@Service）

repository/
├── PhotoRepository.java          # インターフェース
└── impl/
    └── PhotoRepositoryImpl.java  # 実装クラス（@Repository）
```

### レイヤー間のデータ受け渡し

各レイヤー間では専用のオブジェクトを使用してデータを受け渡します。

```mermaid
graph LR
    C["Controller"] -- "Request DTO<br>Response DTO" --> S["Service"]
    S -- "Model<br>オブジェクト" --> R["Repository"]
    R -- "Entity" --> M["Mapper"]
```

| 区間 | 使用するオブジェクト | パッケージ |
|------|----------------------|------------|
| Controller ↔ Service | Request/Response DTO | `controller/request/`, `controller/response/` |
| Service ↔ Repository | Model オブジェクト | `model/` |
| Repository ↔ Mapper | Entity | `entity/` |

### オブジェクト変換

レイヤー間のオブジェクト変換には **ModelMapper** を使用しています。

### コード生成

すべてのEntity・Model・DTOクラスでは **Lombok** アノテーション（`@Getter`, `@Setter`, `@Builder` 等）を使用してボイラープレートコードを削減しています。
