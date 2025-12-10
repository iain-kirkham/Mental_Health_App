import React from 'react';
import PageInset from './PageInset';

interface PageHeaderProps {
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'wide';
  className?: string;
}

export default function PageHeader({ title, subtitle, size = 'wide', className = '' }: PageHeaderProps) {
  return (
    <header className={`w-full ${className}`}>
      {/* Full-bleed background on mobile; inner content aligned by PageInset */}
      <div className="w-full bg-slate-700">
        <div className="pt-4 pb-6 md:pt-4 md:pb-4">
          <PageInset size={size}>
            <div className="w-full text-center">
              <h1 className="text-2xl md:text-3xl font-bold text-white inline-block">{title}</h1>
              {subtitle && (
                <p className="text-white/90 font-medium mt-1 text-sm md:text-base">{subtitle}</p>
              )}
            </div>
          </PageInset>
        </div>
      </div>
    </header>
  );
}
