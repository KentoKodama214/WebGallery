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

const mockMap = {
  setView: jest.fn(),
  on: jest.fn((event: string, handler: ClickHandler) => {
    if (event === "click") registeredClickHandler = handler;
  }),
  remove: jest.fn(),
  getZoom: jest.fn(() => 5),
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

  it("地図クリックで緯度経度・逆ジオコーディングした住所を伴ってonPickが呼ばれること", async () => {
    const onPick = jest.fn();
    render(<LocationMapPicker latitude={null} longitude={null} onPick={onPick} />);

    await waitFor(() => {
      expect(registeredClickHandler).toBeDefined();
    });

    await registeredClickHandler!({ latlng: { lat: 35.6812, lng: 139.7671 } });

    await waitFor(() => {
      expect(onPick).toHaveBeenCalledWith(35.6812, 139.7671, "東京都渋谷区");
    });
  });

  it("逆ジオコーディングに失敗しても、住所null・緯度経度は設定されてonPickが呼ばれること", async () => {
    global.fetch = jest.fn().mockRejectedValue(new Error("network error")) as unknown as typeof fetch;
    const onPick = jest.fn();
    render(<LocationMapPicker latitude={null} longitude={null} onPick={onPick} />);

    await waitFor(() => {
      expect(registeredClickHandler).toBeDefined();
    });

    await registeredClickHandler!({ latlng: { lat: 35.0, lng: 139.0 } });

    await waitFor(() => {
      expect(onPick).toHaveBeenCalledWith(35.0, 139.0, null);
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
