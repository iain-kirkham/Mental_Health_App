/** @type {import('next').NextConfig} */
// Enable the standalone output when not building on Windows so the
// Docker build (Linux) produces the `/app/.next/standalone` folder expected
// by the Dockerfile. Keep it disabled for local Windows builds to avoid
// symlink EPERM issues.
module.exports = {
  output: process.platform === 'win32' ? undefined : 'standalone',
};