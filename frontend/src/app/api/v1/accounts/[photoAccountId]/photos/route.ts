import { type NextRequest, NextResponse } from "next/server";

const BACKEND_URL = process.env.BACKEND_URL || "http://localhost:8080";

/**
 * 写真保存APIのプロキシ（POST/PUT）
 * Next.jsのrewritesではmultipart/form-dataが正しくプロキシされないため、
 * APIルートで明示的にプロキシする
 */
async function proxyToBackend(
  request: NextRequest,
  params: Promise<{ photoAccountId: string }>,
  method: string
) {
  const { photoAccountId } = await params;
  const url = `${BACKEND_URL}/api/v1/accounts/${photoAccountId}/photos`;

  const headers = new Headers();
  const authorization = request.headers.get("Authorization");
  if (authorization) {
    headers.set("Authorization", authorization);
  }
  const contentType = request.headers.get("Content-Type");
  if (contentType) {
    headers.set("Content-Type", contentType);
  }
  headers.set("Accept", "application/json");

  const body = await request.arrayBuffer();

  const response = await fetch(url, {
    method,
    headers,
    body,
  });

  const responseBody = await response.arrayBuffer();
  return new NextResponse(responseBody, {
    status: response.status,
    headers: {
      "Content-Type": response.headers.get("Content-Type") || "application/json",
    },
  });
}

export async function POST(
  request: NextRequest,
  context: { params: Promise<{ photoAccountId: string }> }
) {
  return proxyToBackend(request, context.params, "POST");
}

export async function PUT(
  request: NextRequest,
  context: { params: Promise<{ photoAccountId: string }> }
) {
  return proxyToBackend(request, context.params, "PUT");
}
