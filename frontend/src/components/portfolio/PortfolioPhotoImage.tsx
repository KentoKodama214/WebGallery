import type { CSSProperties } from "react";
import { sanitizeImageUrl } from "@/lib/url";
import type { PublicPhoto } from "@/lib/portfolio-api/types";

interface PortfolioPhotoImageProps {
  photo: PublicPhoto;
  className?: string;
  style?: CSSProperties;
}

/**
 * 公開ページの写真
 *
 * 画像URLは許可したオリジンのものだけを表示する（`sanitizeImageUrl`。CSP の `img-src` と一致させる）。
 * 表示できない場合は、写真のタイトルを代わりに表示する。
 */
export function PortfolioPhotoImage({ photo, className, style }: PortfolioPhotoImageProps) {
  const src = sanitizeImageUrl(photo.url);
  const alt = photo.japaneseTitle || photo.englishTitle;
  if (!src) {
    return (
      <div
        className={`${className ?? ""} bg-gray-200 flex items-center justify-center text-sm text-gray-700`}
        style={style}
      >
        {alt}
      </div>
    );
  }
  return <img src={src} alt={alt} className={className} style={style} loading="lazy" />;
}
