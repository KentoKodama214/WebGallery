"use client";

import { useEffect, useRef } from "react";
import "leaflet/dist/leaflet.css";

/** 地図の初期表示位置（東京駅付近）。緯度経度が未指定の場合に使用する */
const DEFAULT_CENTER: [number, number] = [35.6812, 139.7671];
const DEFAULT_ZOOM = 5;
const PICKED_ZOOM = 15;

/** ピン設置後に逆ジオコーディングを実行するまでの待機時間（ミリ秒） */
const REVERSE_GEOCODE_DEBOUNCE_MS = 400;

/**
 * 緯度・経度の小数桁数
 *
 * DBの列定義（`common.location_mst.latitude` / `longitude` が `decimal(11,4)`）と、入力欄の
 * `step="0.0001"` に合わせる。Leafletがクリック位置から返す値は小数十数桁あり、そのまま
 * 入力欄へ入れると（1）保存時にDB側で黙って丸められて表示値と食い違う（2）`step` に
 * 一致しないためブラウザの制約検証で送信が止まる、という2点の不整合が起きる
 */
const GEO_COORDINATE_DECIMALS = 4;

/**
 * 緯度・経度を保存時の精度（小数第4位）へ丸める
 *
 * @param value 丸める前の値
 * @returns 小数第4位へ丸めた値
 */
function roundCoordinate(value: number): number {
  return Number(value.toFixed(GEO_COORDINATE_DECIMALS));
}

/** ピンのSVGアイコン（標準のマーカー画像はバンドラー経由の読み込み設定が必要なためdivIconで代替する） */
const PIN_ICON_HTML = `<svg width="24" height="32" viewBox="0 0 24 32" xmlns="http://www.w3.org/2000/svg">
  <path d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20c0-6.6-5.4-12-12-12z" fill="#e11d48"/>
  <circle cx="12" cy="12" r="5" fill="#fff"/>
</svg>`;

interface LocationMapPickerProps {
  latitude: number | null;
  longitude: number | null;
  /**
   * 地図クリックでピンを設置した際に呼ばれる
   *
   * 逆ジオコーディングは外部サービスへの通信を伴うため完了を待たず、緯度・経度のみを即座に通知する。
   * 住所は {@link LocationMapPickerProps.onAddressResolved} で別途通知するため、このコールバックが
   * 座標を通知するのはクリックの瞬間の1回だけになる
   */
  onPick: (latitude: number, longitude: number) => void;
  /**
   * 逆ジオコーディングで住所が取得できた際に呼ばれる
   *
   * 座標は通知しない。`onPick` で座標を再通知すると、通信を待っている間にユーザーが
   * 緯度・経度を手入力した場合にクリック位置へ巻き戻してしまうため
   */
  onAddressResolved?: (address: string) => void;
  /** 地図の説明文を持つ要素の id（`aria-describedby` に設定する） */
  describedById?: string;
}

/**
 * 地図クリックで緯度経度を取得するロケーション選択コンポーネント（OpenStreetMap / Leaflet使用）
 *
 * ピン設置時、OpenStreetMap Nominatimの逆ジオコーディングAPIで住所を自動補完する（ベストエフォート。
 * 失敗しても緯度経度の設定自体は成功として扱う）。
 *
 * 逆ジオコーディングは以下の制御を行う。
 * - 緯度・経度の反映は通信の完了を待たない（低速回線でも即座に入力欄へ反映される）
 * - 連続クリック時は前回のリクエストを中断し、デバウンス後に最後の座標だけを問い合わせる
 *   （後着の古い応答が新しいクリックの結果を上書きするのを防ぐ。Nominatimの利用ポリシー上も
 *   クリックごとに無制限へ投げるのは避ける）
 * - 応答で通知するのは住所だけに限る（座標を再通知すると、通信中に手入力した値を巻き戻す）
 */
export function LocationMapPicker({
  latitude,
  longitude,
  onPick,
  onAddressResolved,
  describedById,
}: LocationMapPickerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  // Leafletの型はサーバー実行時に評価されないよう、any的に緩く保持する
  const mapRef = useRef<import("leaflet").Map | null>(null);
  const markerRef = useRef<import("leaflet").Marker | null>(null);
  const onPickRef = useRef(onPick);
  const onAddressResolvedRef = useRef(onAddressResolved);
  // 進行中の逆ジオコーディングを中断するためのコントローラとデバウンスタイマー
  const geocodeAbortRef = useRef<AbortController | null>(null);
  const geocodeTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    onPickRef.current = onPick;
  }, [onPick]);

  useEffect(() => {
    onAddressResolvedRef.current = onAddressResolved;
  }, [onAddressResolved]);

  // アンマウント時に進行中の逆ジオコーディングとデバウンスタイマーを後片付けする
  useEffect(() => {
    return () => {
      geocodeAbortRef.current?.abort();
      geocodeAbortRef.current = null;
      if (geocodeTimerRef.current !== null) {
        clearTimeout(geocodeTimerRef.current);
        geocodeTimerRef.current = null;
      }
    };
  }, []);

  /**
   * 逆ジオコーディングをデバウンスして実行する
   *
   * 進行中のリクエストは中断するため、応答が返るのは常に最後のクリック分だけになる。
   * 通知するのは住所のみで、座標は通知しない（通信中に手入力された緯度・経度を壊さないため）
   *
   * @param lat 緯度
   * @param lng 経度
   */
  const scheduleReverseGeocode = (lat: number, lng: number) => {
    geocodeAbortRef.current?.abort();
    if (geocodeTimerRef.current !== null) {
      clearTimeout(geocodeTimerRef.current);
    }
    geocodeTimerRef.current = setTimeout(() => {
      geocodeTimerRef.current = null;
      const controller = new AbortController();
      geocodeAbortRef.current = controller;
      fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`, {
        signal: controller.signal,
      })
        .then((response) => (response.ok ? response.json() : null))
        .then((data) => {
          if (controller.signal.aborted) return;
          const address = typeof data?.display_name === "string" ? data.display_name : null;
          if (address) onAddressResolvedRef.current?.(address);
        })
        .catch(() => {
          // 逆ジオコーディングの失敗・中断は無視する（緯度経度の設定自体は成功として扱う）
        })
        .finally(() => {
          if (geocodeAbortRef.current === controller) {
            geocodeAbortRef.current = null;
          }
        });
    }, REVERSE_GEOCODE_DEBOUNCE_MS);
  };

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    let cancelled = false;
    import("leaflet").then((L) => {
      if (cancelled || !containerRef.current || mapRef.current) return;

      // 入力途中の値（`Number("-")`等）はNaNになりLeafletが例外を投げるため、有限数のみ採用する
      const hasInitialPosition = Number.isFinite(latitude) && Number.isFinite(longitude);
      const initialCenter: [number, number] = hasInitialPosition
        ? [latitude as number, longitude as number]
        : DEFAULT_CENTER;
      const map = L.map(containerRef.current).setView(
        initialCenter,
        hasInitialPosition ? PICKED_ZOOM : DEFAULT_ZOOM
      );
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19,
      }).addTo(map);

      const pinIcon = L.divIcon({
        className: "",
        html: PIN_ICON_HTML,
        iconSize: [24, 32],
        iconAnchor: [12, 32],
      });

      if (hasInitialPosition) {
        markerRef.current = L.marker(initialCenter, { icon: pinIcon }).addTo(map);
      }

      map.on("click", (e: import("leaflet").LeafletMouseEvent) => {
        // 入力欄・DBの精度（小数第4位）に合わせて丸めた値を、ピン位置にも通知値にも用いる
        const lat = roundCoordinate(e.latlng.lat);
        const lng = roundCoordinate(e.latlng.lng);
        if (markerRef.current) {
          markerRef.current.setLatLng([lat, lng]);
        } else {
          markerRef.current = L.marker([lat, lng], { icon: pinIcon }).addTo(map);
        }

        // 緯度・経度は外部通信の完了を待たず即座に反映する
        onPickRef.current(lat, lng);
        scheduleReverseGeocode(lat, lng);
      });

      mapRef.current = map;
    });

    return () => {
      cancelled = true;
      mapRef.current?.remove();
      mapRef.current = null;
      markerRef.current = null;
    };
    // 初回マウント時のみ初期化する（緯度経度の外部からの変更は下のuseEffectで反映する）
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 数値入力欄側からの変更等、外部要因での緯度経度の変化を地図・ピンに反映する
  useEffect(() => {
    // 入力途中の値（`Number("-")`等）はNaNになりLeafletが例外を投げるため、有限数のみ反映する
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) return;
    const position: [number, number] = [latitude as number, longitude as number];

    // 動的importの解決前にアンマウント・依存変更でクリーンアップが走った場合は、
    // 破棄済みのMapへ操作して例外になるため中断する
    let cancelled = false;
    import("leaflet").then((L) => {
      const map = mapRef.current;
      if (cancelled || !map) return;

      const isFirstPin = markerRef.current === null;
      if (markerRef.current) {
        markerRef.current.setLatLng(position);
      } else {
        const pinIcon = L.divIcon({
          className: "",
          html: PIN_ICON_HTML,
          iconSize: [24, 32],
          iconAnchor: [12, 32],
        });
        markerRef.current = L.marker(position, { icon: pinIcon }).addTo(map);
      }

      // 緯度・経度を手入力している最中に毎回ズームし直すと、ユーザーがズームアウトできない。
      // 初回のピン設置時、または現在の表示範囲から外れた時だけ地図を追従させる
      if (isFirstPin) {
        map.setView(position, Math.max(map.getZoom(), PICKED_ZOOM));
      } else if (!map.getBounds().contains(position)) {
        map.setView(position, map.getZoom());
      }
    });

    return () => {
      cancelled = true;
    };
  }, [latitude, longitude]);

  // role="application" は「ウィジェット自身がキーボード操作を提供する」場合のロールであり、
  // クリックでしかピンを置けないこのコンポーネントには不適切（AT固有のキー操作を奪うだけになる）。
  // キーボードの代替手段は緯度・経度の直接入力（説明文で案内）なので、group + ラベルに留める
  return (
    <div
      ref={containerRef}
      role="group"
      aria-label="撮影場所を選択する地図（クリックで緯度・経度を設定します）"
      aria-describedby={describedById}
      className="w-full h-64 border border-gray-600"
      data-testid="location-map-picker"
    />
  );
}
