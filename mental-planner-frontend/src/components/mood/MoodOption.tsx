import React from "react";
import { Button } from "@/components/ui/button";

type Props = {
  value: number;
  label: string;
  icon: React.ReactNode;
  colorClass: string;
  selected: boolean;
  onSelect: (v: number) => void;
  disabled?: boolean;
};

export default function MoodOption({ value, label, icon, colorClass, selected, onSelect, disabled }: Props) {
  return (
    <Button
      key={value}
      variant="outline"
      className={`flex flex-col items-center p-4 min-h-20 flex-1 transition-all duration-300 hover:scale-105 hover:shadow-md ${
        selected ? `${colorClass} border-2 scale-110 shadow-lg` : "border-2 border-slate-200 dark:border-slate-700 hover:border-slate-300"
      }`}
      onClick={() => onSelect(value)}
      aria-label={`Select mood: ${label}`}
      aria-pressed={selected}
      disabled={disabled}
    >
      <div className={`text-3xl transition-transform duration-300 ${selected ? "scale-125" : "scale-100 opacity-70"}`}>
        {icon}
      </div>
      <span className={`text-xs mt-2 font-medium ${selected ? "font-bold" : ""}`}>{label}</span>
    </Button>
  );
}

