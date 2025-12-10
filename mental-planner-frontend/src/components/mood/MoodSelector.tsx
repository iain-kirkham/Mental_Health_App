import React from "react";
import type { MoodOption as MoodOptionType } from "@/types";
import MoodOption from "./MoodOption";
import { Frown, Smile, Meh } from "lucide-react";

const MOOD_OPTIONS: MoodOptionType[] = [
	{
		value: 1,
		icon: <Frown size={32} strokeWidth={2.5} />,
		label: "Very Bad",
		color: "bg-red-100 hover:bg-red-200 border-red-300",
	},
	{
		value: 2,
		icon: <Frown size={32} />,
		label: "Bad",
		color: "bg-orange-100 hover:bg-orange-200 border-orange-300",
	},
	{
		value: 3,
		icon: <Meh size={32} />,
		label: "Okay",
		color: "bg-yellow-100 hover:bg-yellow-200 border-yellow-300",
	},
	{
		value: 4,
		icon: <Smile size={32} />,
		label: "Good",
		color: "bg-green-100 hover:bg-green-200 border-green-300",
	},
	{
		value: 5,
		icon: <Smile size={32} strokeWidth={2.5} />,
		label: "Very Good",
		color: "bg-emerald-100 hover:bg-emerald-200 border-emerald-300",
	},
];

type Props = {
	selectedMood: number | null;
	setSelectedMood: (v: number | null) => void;
	isSubmitting: boolean;
};

export default function MoodSelector({
	selectedMood,
	setSelectedMood,
	isSubmitting,
}: Props) {
	return (
		<div
			className="flex justify-between items-center gap-2 py-2"
			role="group"
			aria-label="Mood selection"
		>
			{MOOD_OPTIONS.map((mood) => (
				<MoodOption
					key={mood.value}
					value={mood.value}
					label={mood.label}
					icon={mood.icon}
					colorClass={mood.color}
					selected={selectedMood === mood.value}
					onSelect={(v) => setSelectedMood(v)}
					disabled={isSubmitting}
				/>
			))}
		</div>
	);
}
