/** @type {import('next').NextConfig} */
// Standalone output is only for a self-hosted Docker build (set DOCKER_BUILD=true
// there). Vercel has its own tracing/bundling pipeline and breaks if this is set,
// so it must stay unset for Vercel (and local) builds.
module.exports = {
  output: process.env.DOCKER_BUILD === 'true' ? 'standalone' : undefined,
};