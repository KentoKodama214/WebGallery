"use client";

import { useEffect, useRef } from "react";
import "leaflet/dist/leaflet.css";

/** 地図の初期表示位置（東京駅付近）。緯度経度が未指定の場合に使用する */
const DEFAULT_CENTER: [number, number] = [35.6812, 139.7671];
const DEFAULT_ZOOM = 5;
const PICKED_ZOOM = 15;

interface LocationMapPickerProps {
  latitude: number | null;
  longitude: number | null;
  /** 地図クリックでピンを設置した際に呼ばれる。逆ジオコーディングに成功した場合、住所も渡す */
  onPick: (latitude: number, longitude: number, address: string | null) => void;
}

/**
 * 地図クリックで緯度経度を取得するロケーション選択コンポーネント（OpenStreetMap / Leaflet使用）
 *
 * ピン設置時、OpenStreetMap Nominatimの逆ジオコーディングAPIで住所を自動補完する（ベストエフォート。
 * 失敗しても緯度経度の設定自体は成功として扱う）
 */
export function LocationMapPicker({ latitude, longitude, onPick }: LocationMapPickerProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  // Leafletの型はサーバー実行時に評価されないよう、any的に緩く保持する
  const mapRef = useRef<import("leaflet").Map | null>(null);
  const markerRef = useRef<import("leaflet").Marker | null>(null);
  const onPickRef = useRef(onPick);
  useEffect(() => {
    onPickRef.current = onPick;
  }, [onPick]);

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    let cancelled = false;
    import("leaflet").then((L) => {
      if (cancelled || !containerRef.current || mapRef.current) return;

      const initialCenter: [number, number] =
        latitude != null && longitude != null ? [latitude, longitude] : DEFAULT_CENTER;
      const map = L.map(containerRef.current).setView(
        initialCenter,
        latitude != null && longitude != null ? PICKED_ZOOM : DEFAULT_ZOOM
      );
      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19,
      }).addTo(map);

      // 標準のマーカー画像はバンドラー経由の読み込み設定が別途必要になるため、
      // 依存を増やさずインラインSVGのdivIconで代替する
      const pinIcon = L.divIcon({
        className: "",
        html: `<svg width="24" height="32" viewBox="0 0 24 32" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20c0-6.6-5.4-12-12-12z" fill="#e11d48"/>
          <circle cx="12" cy="12" r="5" fill="#fff"/>
        </svg>`,
        iconSize: [24, 32],
        iconAnchor: [12, 32],
      });

      if (latitude != null && longitude != null) {
        markerRef.current = L.marker([latitude, longitude], { icon: pinIcon }).addTo(map);
      }

      map.on("click", async (e: import("leaflet").LeafletMouseEvent) => {
        const { lat, lng } = e.latlng;
        if (markerRef.current) {
          markerRef.current.setLatLng([lat, lng]);
        } else {
          markerRef.current = L.marker([lat, lng], { icon: pinIcon }).addTo(map);
        }

        let address: string | null = null;
        try {
          const response = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`
          );
          if (response.ok) {
            const data = await response.json();
            address = typeof data?.display_name === "string" ? data.display_name : null;
          }
        } catch {
          // 逆ジオコーディングの失敗は無視する（緯度経度の設定自体は成功として扱う）
        }
        onPickRef.current(lat, lng, address);
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
    const map = mapRef.current;
    if (!map || latitude == null || longitude == null) return;

    import("leaflet").then((L) => {
      if (markerRef.current) {
        markerRef.current.setLatLng([latitude, longitude]);
      } else {
        const pinIcon = L.divIcon({
          className: "",
          html: `<svg width="24" height="32" viewBox="0 0 24 32" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 0C5.4 0 0 5.4 0 12c0 9 12 20 12 20s12-11 12-20c0-6.6-5.4-12-12-12z" fill="#e11d48"/>
            <circle cx="12" cy="12" r="5" fill="#fff"/>
          </svg>`,
          iconSize: [24, 32],
          iconAnchor: [12, 32],
        });
        markerRef.current = L.marker([latitude, longitude], { icon: pinIcon }).addTo(map);
      }
      map.setView([latitude, longitude], Math.max(map.getZoom(), PICKED_ZOOM));
    });
  }, [latitude, longitude]);

  return (
    <div
      ref={containerRef}
      className="w-full h-64 border border-gray-600"
      data-testid="location-map-picker"
    />
  );
}
