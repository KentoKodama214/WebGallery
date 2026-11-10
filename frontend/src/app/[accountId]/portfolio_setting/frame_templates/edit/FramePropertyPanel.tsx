"use client";

import { useId, useState } from "react";
import {
  FRAME_FONT_OPTIONS,
  FRAME_LIMITS,
  FRAME_MESSAGE_MAX_LENGTH,
  FRAME_SIZE_PRESETS,
  FRAME_TEXT_ALIGN_OPTIONS,
  clamp,
  findFrameSizePresetIndex,
  round,
  type FrameDefinition,
  type FrameFontFamily,
  type FrameSlot,
  type FrameText,
  type FrameTextAlign,
} from "@/lib/frame/definition";
import type { FrameSelection } from "./FrameEditorCanvas";

interface NumberFieldProps {
  label: string;
  value: number;
  min: number;
  max: number;
  step?: number;
  /** 単位の表示（%・mm・° 等） */
  unit?: string;
  onChange: (value: number) => void;
}

/**
 * 数値の入力欄
 *
 * 入力中は入力された文字をそのまま表示し（空欄・範囲外の途中の値も打てるようにする）、範囲内の数値になった時点で定義に反映する。
 * フォーカスを外すか Enter キーで確定し、範囲外の値は範囲内に収め、数値として解釈できない値は元に戻す。
 */
function NumberField({ label, value, min, max, step = 0.1, unit, onChange }: NumberFieldProps) {
  const id = useId();
  /**
   * 入力中の文字と、その時点で定義に反映済みの値（入力中でなければ null とし、定義の値を表示する）。
   * 入力中にキャンバスの操作等で値が変わった場合は、古い入力で上書きしないよう入力中の文字を捨てる
   */
  const [draft, setDraft] = useState<{ text: string; base: number } | null>(null);
  const draftText = draft !== null && draft.base === value ? draft.text : null;

  const commit = () => {
    if (draftText === null) {
      setDraft(null);
      return;
    }
    const parsed = Number(draftText);
    if (draftText.trim() !== "" && Number.isFinite(parsed)) {
      onChange(round(clamp(parsed, min, max, value), 2));
    }
    setDraft(null);
  };

  return (
    <div className="flex flex-col">
      <label htmlFor={id} className="text-xs text-gray-700 mb-0.5">
        {label}
        {unit && <span className="ml-0.5">（{unit}）</span>}
      </label>
      <input
        id={id}
        type="number"
        value={draftText ?? value}
        min={min}
        max={max}
        step={step}
        onChange={(e) => {
          const text = e.target.value;
          const parsed = Number(text);
          if (text.trim() !== "" && Number.isFinite(parsed) && parsed >= min && parsed <= max) {
            const next = round(parsed, 2);
            setDraft({ text, base: next });
            onChange(next);
            return;
          }
          setDraft({ text, base: value });
        }}
        onBlur={commit}
        onKeyDown={(e) => {
          if (e.key === "Enter") commit();
        }}
        className="w-full border border-gray-300 rounded px-2 py-1 text-sm"
      />
    </div>
  );
}

interface ColorFieldProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
}

/** 色の入力欄 */
function ColorField({ label, value, onChange }: ColorFieldProps) {
  const id = useId();
  return (
    <div className="flex items-center justify-between gap-2">
      <label htmlFor={id} className="text-sm">
        {label}
      </label>
      <input
        id={id}
        type="color"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-12 h-8 border border-gray-300 rounded cursor-pointer"
      />
    </div>
  );
}

interface FramePropertyPanelProps {
  definition: FrameDefinition;
  selection: FrameSelection | null;
  /** メッセージ枠に表示する確認用の文面（保存しない） */
  sampleTexts: string[];
  onChangeDefinition: (updater: (prev: FrameDefinition) => FrameDefinition) => void;
  onChangeSlot: (index: number, change: Partial<FrameSlot>) => void;
  onChangeText: (index: number, change: Partial<FrameText>) => void;
  onChangeSampleText: (index: number, value: string) => void;
  onDuplicate: (selection: FrameSelection) => void;
  onDelete: (selection: FrameSelection) => void;
}

/**
 * 額縁テンプレートの設定パネル
 *
 * 何も選択していない間は額縁全体（サイズ・背景・外枠）の設定、写真枠・メッセージ枠を選択している間は
 * その位置・大きさ・傾き・デザインの設定を表示する。キャンバスでのドラッグ操作の微調整にも使う。
 */
export function FramePropertyPanel({
  definition,
  selection,
  sampleTexts,
  onChangeDefinition,
  onChangeSlot,
  onChangeText,
  onChangeSampleText,
  onDuplicate,
  onDelete,
}: FramePropertyPanelProps) {
  const isPortrait = definition.size.height > definition.size.width;
  const presetIndex = findFrameSizePresetIndex(definition.size);
  const sizePresetId = useId();
  const fontId = useId();
  const alignId = useId();
  const sampleId = useId();

  if (selection?.kind === "slot" && definition.slots[selection.index]) {
    const slot = definition.slots[selection.index];
    const index = selection.index;
    return (
      <section aria-labelledby="frame-panel-heading" className="flex flex-col gap-3">
        <h2 id="frame-panel-heading" className="font-bold">
          写真枠{index + 1}
        </h2>
        <p className="text-xs text-gray-600">
          ポートフォリオでは、表示順の{index + 1}枚目の写真がこの枠に当てはまります。
        </p>
        <div className="grid grid-cols-2 gap-2">
          <NumberField label="左" unit="%" value={slot.x} min={0} max={100} onChange={(x) => onChangeSlot(index, { x })} />
          <NumberField label="上" unit="%" value={slot.y} min={0} max={100} onChange={(y) => onChangeSlot(index, { y })} />
          <NumberField
            label="横幅"
            unit="%"
            value={slot.width}
            min={FRAME_LIMITS.boxSizeMin}
            max={100}
            onChange={(width) => onChangeSlot(index, { width })}
          />
          <NumberField
            label="縦幅"
            unit="%"
            value={slot.height}
            min={FRAME_LIMITS.boxSizeMin}
            max={100}
            onChange={(height) => onChangeSlot(index, { height })}
          />
        </div>
        <NumberField
          label="傾き"
          unit="°"
          value={slot.rotation}
          min={-FRAME_LIMITS.rotationMax}
          max={FRAME_LIMITS.rotationMax}
          step={0.5}
          onChange={(rotation) => onChangeSlot(index, { rotation })}
        />
        <ColorField label="枠線の色" value={slot.borderColor} onChange={(borderColor) => onChangeSlot(index, { borderColor })} />
        <NumberField
          label="枠線の太さ"
          unit="%"
          value={slot.borderWidth}
          min={0}
          max={FRAME_LIMITS.slotBorderWidthMax}
          onChange={(borderWidth) => onChangeSlot(index, { borderWidth })}
        />
        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={slot.shadow}
            onChange={(e) => onChangeSlot(index, { shadow: e.target.checked })}
          />
          影を付ける
        </label>
        <ElementActions selection={selection} onDuplicate={onDuplicate} onDelete={onDelete} />
      </section>
    );
  }

  if (selection?.kind === "text" && definition.texts[selection.index]) {
    const text = definition.texts[selection.index];
    const index = selection.index;
    return (
      <section aria-labelledby="frame-panel-heading" className="flex flex-col gap-3">
        <h2 id="frame-panel-heading" className="font-bold">
          メッセージ枠{index + 1}
        </h2>
        <p className="text-xs text-gray-600">
          文面はポートフォリオごとに入力します。ここで入力する文面は見た目の確認用で、保存されません。
        </p>
        <div className="flex flex-col">
          <label htmlFor={sampleId} className="text-xs text-gray-700 mb-0.5">
            確認用の文面
          </label>
          <textarea
            id={sampleId}
            rows={2}
            maxLength={FRAME_MESSAGE_MAX_LENGTH}
            value={sampleTexts[index] ?? ""}
            placeholder={`メッセージ${index + 1}`}
            onChange={(e) => onChangeSampleText(index, e.target.value)}
            className="w-full border border-gray-300 rounded px-2 py-1 text-sm"
          />
        </div>
        <div className="grid grid-cols-2 gap-2">
          <NumberField label="左" unit="%" value={text.x} min={0} max={100} onChange={(x) => onChangeText(index, { x })} />
          <NumberField label="上" unit="%" value={text.y} min={0} max={100} onChange={(y) => onChangeText(index, { y })} />
          <NumberField
            label="横幅"
            unit="%"
            value={text.width}
            min={FRAME_LIMITS.boxSizeMin}
            max={100}
            onChange={(width) => onChangeText(index, { width })}
          />
          <NumberField
            label="傾き"
            unit="°"
            value={text.rotation}
            min={-FRAME_LIMITS.rotationMax}
            max={FRAME_LIMITS.rotationMax}
            step={0.5}
            onChange={(rotation) => onChangeText(index, { rotation })}
          />
        </div>
        <div className="flex flex-col">
          <label htmlFor={fontId} className="text-xs text-gray-700 mb-0.5">
            フォント
          </label>
          <select
            id={fontId}
            value={text.fontFamily}
            onChange={(e) => onChangeText(index, { fontFamily: e.target.value as FrameFontFamily })}
            className="border border-gray-300 rounded px-2 py-1 text-sm"
          >
            {FRAME_FONT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value} style={{ fontFamily: option.css }}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <NumberField
          label="文字サイズ"
          unit="%"
          value={text.fontSize}
          min={FRAME_LIMITS.fontSizeMin}
          max={FRAME_LIMITS.fontSizeMax}
          onChange={(fontSize) => onChangeText(index, { fontSize })}
        />
        <ColorField label="文字の色" value={text.color} onChange={(color) => onChangeText(index, { color })} />
        <div className="flex flex-col">
          <label htmlFor={alignId} className="text-xs text-gray-700 mb-0.5">
            揃え
          </label>
          <select
            id={alignId}
            value={text.align}
            onChange={(e) => onChangeText(index, { align: e.target.value as FrameTextAlign })}
            className="border border-gray-300 rounded px-2 py-1 text-sm"
          >
            {FRAME_TEXT_ALIGN_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={text.bold}
            onChange={(e) => onChangeText(index, { bold: e.target.checked })}
          />
          太字
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={text.italic}
            onChange={(e) => onChangeText(index, { italic: e.target.checked })}
          />
          斜体
        </label>
        <ElementActions selection={selection} onDuplicate={onDuplicate} onDelete={onDelete} />
      </section>
    );
  }

  return (
    <section aria-labelledby="frame-panel-heading" className="flex flex-col gap-3">
      <h2 id="frame-panel-heading" className="font-bold">
        額縁
      </h2>
      <div className="flex flex-col">
        <label htmlFor={sizePresetId} className="text-xs text-gray-700 mb-0.5">
          サイズ
        </label>
        <select
          id={sizePresetId}
          value={presetIndex}
          onChange={(e) => {
            const preset = FRAME_SIZE_PRESETS[Number(e.target.value)];
            if (!preset) return;
            if (preset.oriented) {
              onChangeDefinition((prev) => ({ ...prev, size: { ...preset.size } }));
              return;
            }
            const long = Math.max(preset.size.width, preset.size.height);
            const short = Math.min(preset.size.width, preset.size.height);
            onChangeDefinition((prev) => ({
              ...prev,
              size: isPortrait ? { width: short, height: long } : { width: long, height: short },
            }));
          }}
          className="border border-gray-300 rounded px-2 py-1 text-sm"
        >
          {presetIndex === -1 && <option value={-1}>カスタム</option>}
          {FRAME_SIZE_PRESETS.map((preset, index) => (
            <option key={preset.label} value={index}>
              {preset.label}
            </option>
          ))}
        </select>
      </div>
      <fieldset className="flex gap-4 text-sm">
        <legend className="sr-only">向き</legend>
        {[
          { portrait: false, label: "横長" },
          { portrait: true, label: "縦長" },
        ].map((option) => (
          <label key={option.label} className="flex items-center gap-1">
            <input
              type="radio"
              name="frame-orientation"
              checked={isPortrait === option.portrait}
              onChange={() => {
                if (isPortrait === option.portrait) return;
                onChangeDefinition((prev) => ({
                  ...prev,
                  size: { width: prev.size.height, height: prev.size.width },
                }));
              }}
            />
            {option.label}
          </label>
        ))}
      </fieldset>
      <div className="grid grid-cols-2 gap-2">
        <NumberField
          label="横"
          unit="mm"
          step={1}
          value={definition.size.width}
          min={FRAME_LIMITS.sizeMin}
          max={FRAME_LIMITS.sizeMax}
          onChange={(width) => onChangeDefinition((prev) => ({ ...prev, size: { ...prev.size, width } }))}
        />
        <NumberField
          label="縦"
          unit="mm"
          step={1}
          value={definition.size.height}
          min={FRAME_LIMITS.sizeMin}
          max={FRAME_LIMITS.sizeMax}
          onChange={(height) => onChangeDefinition((prev) => ({ ...prev, size: { ...prev.size, height } }))}
        />
      </div>
      <ColorField
        label="背景（台紙）の色"
        value={definition.background.color}
        onChange={(color) => onChangeDefinition((prev) => ({ ...prev, background: { color } }))}
      />
      <ColorField
        label="外枠の色"
        value={definition.border.color}
        onChange={(color) => onChangeDefinition((prev) => ({ ...prev, border: { ...prev.border, color } }))}
      />
      <NumberField
        label="外枠の太さ"
        unit="%"
        value={definition.border.width}
        min={0}
        max={FRAME_LIMITS.borderWidthMax}
        onChange={(width) => onChangeDefinition((prev) => ({ ...prev, border: { ...prev.border, width } }))}
      />
      <p className="text-xs text-gray-600">
        写真枠・メッセージ枠をクリックすると、その枠の設定を表示します。枠はドラッグで移動、右下のつまみで大きさの変更、上のつまみで回転できます（Shiftキーで15°刻み）。選択中の枠は矢印キーでも移動できます。
      </p>
    </section>
  );
}

/** 選択中の写真枠・メッセージ枠の複製・削除ボタン */
function ElementActions({
  selection,
  onDuplicate,
  onDelete,
}: {
  selection: FrameSelection;
  onDuplicate: (selection: FrameSelection) => void;
  onDelete: (selection: FrameSelection) => void;
}) {
  return (
    <div className="flex gap-2 pt-2 border-t border-gray-200">
      <button
        type="button"
        onClick={() => onDuplicate(selection)}
        className="px-3 py-1 text-sm border border-blue-700 text-blue-700 rounded hover:bg-blue-50"
      >
        複製
      </button>
      <button
        type="button"
        onClick={() => onDelete(selection)}
        className="px-3 py-1 text-sm border border-red-500 text-red-700 rounded hover:bg-red-50"
      >
        削除
      </button>
    </div>
  );
}
