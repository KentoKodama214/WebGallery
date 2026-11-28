import type { FeatureLayout } from "@/lib/portfolio-api/types";
import { PortfolioPhotoImage } from "../PortfolioPhotoImage";

/**
 * フィーチャー型テンプレート
 *
 * 写真1枚ごとに、写真とテーマ・サブテーマ・説明を持つセクションを縦に並べる。
 * 広い画面では写真と文章を左右に並べ、セクションごとに左右を入れ替える。
 */
export function FeatureTemplate({ layout }: { layout: FeatureLayout }) {
  return (
    <div className="flex flex-col gap-16" data-testid="feature-template">
      {layout.sections.map((section, index) => (
        <section
          key={section.photo.photoNo}
          data-testid="feature-section"
          className={`flex flex-col gap-6 md:items-center ${index % 2 === 1 ? "md:flex-row-reverse" : "md:flex-row"}`}
        >
          <PortfolioPhotoImage photo={section.photo} className="w-full md:w-3/5 object-cover rounded" />
          <div className="md:w-2/5 flex flex-col gap-3">
            <h2 className="text-2xl font-bold">{section.theme}</h2>
            {section.subTheme && <p className="text-lg text-gray-700">{section.subTheme}</p>}
            {section.description && (
              <p className="leading-relaxed whitespace-pre-wrap">{section.description}</p>
            )}
          </div>
        </section>
      ))}
    </div>
  );
}
