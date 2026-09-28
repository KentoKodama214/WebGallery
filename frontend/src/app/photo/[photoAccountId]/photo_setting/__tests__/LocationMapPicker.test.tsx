import { render, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { LocationMapPicker } from "../LocationMapPicker";

type ClickHandler = (e: { latlng: { lat: number; lng: number } }) => void;

const mockMarker = {
  addTo: jest.fn(),
  setLatLng: jest.fn(),
};
mockMarker.addTo.mockReturnValue(mockMarker);

let registeredClickHandler: ClickHandler | undefined;

const mockContains = jest.fn(() => true);

const mockMap = {
  setView: jest.fn(),
  on: jest.fn((event: string, handler: ClickHandler) => {
    if (event === "click") registeredClickHandler = handler;
  }),
  remove: jest.fn(),
  getZoom: jest.fn(() => 5),
  getBounds: jest.fn(() => ({ contains: mockContains })),
};
mockMap.setView.mockReturnValue(mockMap);

const mockTileLayer = { addTo: jest.fn() };

jest.mock("leaflet", () => ({
  map: jest.fn(() => mockMap),
  tileLayer: jest.fn(() => mockTileLayer),
  marker: jest.fn(() => mockMarker),
  divIcon: jest.fn(() => ({})),
}));

describe("LocationMapPicker", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockMap.setView.mockReturnValue(mockMap);
    mockMap.getBounds.mockReturnValue({ contains: mockContains });
    mockContains.mockReturnValue(true);
    mockMarker.addTo.mockReturnValue(mockMarker);
    registeredClickHandler = undefined;
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ display_name: "東京都渋谷区" }),
    }) as unknown as typeof fetch;
  });

  it("マウント時に地図コンテナが表示されること", async () => {
    const onPick = jest.fn();
    const { getByTestId } = render(
      <LocationMapPicker latitude={null} longitude={null} onPick={onPick} />
    );

    expect(getByTestId("location-map-picker")).toBeInTheDocument();
    await waitFor(() => {
      expect(mockMap.on).toHaveBeenCalledWith("click", expect.any(Function));
    });
  });

  it("キーボード操作を提供しないため role=\"application\" ではなく group を用いること", async () => {
    const onPick = jest.fn();
    const { getByTestId } = render(
      <LocationMapPicker latitude={null} longitude={null} onPick={onPick} />
    );

    const container = getByTestId("location-map-picker");
    expect(container).toHaveAttribute("role", "group");
    expect(container).toHaveAccessibleName(
      "撮影場所を選択する地図（クリックで緯度・経度を設定します）"
    );
    await waitFor(() => {
      expect(mockMap.on).toHaveBeenCalled();
    });
  });

  it("地図クリックで緯度経度が即座に通知され、住所は解決後にonAddressResolvedで通知されること", async () => {
    const onPick = jest.fn();
    const onAddressResolved = jest.fn();
    render(
      <LocationMapPicker
        latitude={null}
        longitude={null}
        onPick={onPick}
        onAddressResolved={onAddressResolved}
      />
    );

    await waitFor(() => {
      expect(registeredClickHandler).toBeDefined();
    });

    // Leafletはクリック位置を小数十数桁で返す。入力欄（step="0.0001"）とDBの
    // decimal(11,4) に合わせて小数第4位へ丸めた値が通知されること
    await registeredClickHandler!({
      latlng: { lat: 35.67514743608467, lng: 139.74609375000003 },
    });

    // 座標は通信を待たずに1回だけ通知される
    expect(onPick).toHaveBeenCalledTimes(1);
    expect(onPick).toHaveBeenCalledWith(35.6751, 139.7461);

    await waitFor(() => {
      expect(onAddressResolved).toHaveBeenCalledWith("東京都渋谷区");
    });
    // 住所の解決後も座標を再通知しない（通信中に手入力された緯度・経度を壊さないため）
    expect(onPick).toHaveBeenCalledTimes(1);
  });

  it("逆ジオコーディングに失敗しても緯度経度は通知され、住所は通知されないこと", async () => {
    global.fetch = jest.fn().mockRejectedValue(new Error("network error")) as unknown as typeof fetch;
    const onPick = jest.fn();
    const onAddressResolved = jest.fn();
    render(
      <LocationMapPicker
        latitude={null}
        longitude={null}
        onPick={onPick}
        onAddressResolved={onAddressResolved}
      />
    );

    await waitFor(() => {
      expect(registeredClickHandler).toBeDefined();
    });

    await registeredClickHandler!({ latlng: { lat: 35.0, lng: 139.0 } });

    expect(onPick).toHaveBeenCalledWith(35.0, 139.0);
    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalled();
    });
    expect(onAddressResolved).not.toHaveBeenCalled();
  });

  it("緯度経度が数値として不正な場合はピンを置かないこと", async () => {
    const onPick = jest.fn();
    render(<LocationMapPicker latitude={Number("-")} longitude={Number("-")} onPick={onPick} />);

    await waitFor(() => {
      expect(mockMap.on).toHaveBeenCalled();
    });
    // NaN を渡すと Leaflet が例外を投げるため、マーカー生成・表示移動を行わない
    expect(mockMarker.addTo).not.toHaveBeenCalled();
    expect(mockMap.setView).toHaveBeenCalledWith([35.6812, 139.7671], 5);
  });

  it("ピン設置済みで表示範囲内の座標変更では、地図をズームし直さないこと", async () => {
    const onPick = jest.fn();
    const { rerender } = render(
      <LocationMapPicker latitude={35.6812} longitude={139.7671} onPick={onPick} />
    );

    await waitFor(() => {
      expect(mockMarker.addTo).toHaveBeenCalled();
    });
    mockMap.setView.mockClear();

    // 緯度を手入力で少し動かす（表示範囲内）
    mockContains.mockReturnValue(true);
    rerender(<LocationMapPicker latitude={35.69} longitude={139.7671} onPick={onPick} />);

    await waitFor(() => {
      expect(mockMarker.setLatLng).toHaveBeenCalledWith([35.69, 139.7671]);
    });
    expect(mockMap.setView).not.toHaveBeenCalled();
  });

  it("ピン設置済みで表示範囲外の座標変更では、ズームを維持したまま地図を追従させること", async () => {
    const onPick = jest.fn();
    const { rerender } = render(
      <LocationMapPicker latitude={35.6812} longitude={139.7671} onPick={onPick} />
    );

    await waitFor(() => {
      expect(mockMarker.addTo).toHaveBeenCalled();
    });
    mockMap.setView.mockClear();

    mockContains.mockReturnValue(false);
    rerender(<LocationMapPicker latitude={-33.8688} longitude={151.2093} onPick={onPick} />);

    await waitFor(() => {
      expect(mockMap.setView).toHaveBeenCalledWith([-33.8688, 151.2093], 5);
    });
  });

  it("アンマウント時に地図インスタンスを破棄すること", async () => {
    const onPick = jest.fn();
    const { unmount } = render(
      <LocationMapPicker latitude={null} longitude={null} onPick={onPick} />
    );

    await waitFor(() => {
      expect(mockMap.on).toHaveBeenCalled();
    });

    unmount();
    expect(mockMap.remove).toHaveBeenCalled();
  });
});
