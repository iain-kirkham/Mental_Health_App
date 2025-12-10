import React from 'react';

type Size = 'sm' | 'md' | 'lg' | 'xl' | 'wide';

const sizeMap: Record<Size, string> = {
  sm: 'max-w-sm',
  md: 'max-w-md',
  lg: 'max-w-lg',
  xl: 'max-w-xl',
  wide: 'max-w-5xl',
};

interface PageInsetProps {
  children: React.ReactNode;
  size?: Size;
  className?: string;
}

export default function PageInset({ children, size = 'md', className = '' }: PageInsetProps) {
  const maxClass = sizeMap[size] || sizeMap.md;
  // mobile inset px-6, small desktop padding (md:px-4), remove extra padding on lg+ where container width handles centering
  return (
    <div className={`w-full px-6 md:px-4 lg:px-0 ${maxClass} mx-auto ${className}`}>{children}</div>
  );
}
