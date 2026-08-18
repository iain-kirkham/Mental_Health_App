import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@clerk/nextjs";
import { toast } from "sonner";
import {
  deleteTaskTimeEntry as apiDeleteTaskTimeEntry,
  getTaskTimeEntries as apiGetTaskTimeEntries,
  logTaskTimeEntry as apiLogTaskTimeEntry,
} from "@/lib/tasks-api";
import { toFriendlyMessage } from "@/lib/connectivity";
import type { TaskTimeEntryRequestDTO } from "@/types";

export type ManualTimeEntryInput = {
  entryDate: string;
  minutes: number;
  note: string | null;
};

/**
 * Fetches and mutates a single task's logged time entries (stopwatch runs + manual entries).
 * Scoped to one task at a time, so it's driven from wherever that task's detail view is open.
 */
export default function useTaskTimeEntries(taskId: number | null) {
  const { getToken } = useAuth();
  const queryClient = useQueryClient();
  const queryKey = ["task-time-entries", taskId] as const;

  const query = useQuery({
    queryKey,
    queryFn: () => apiGetTaskTimeEntries(taskId as number, getToken),
    enabled: taskId !== null,
  });

  const addManualEntry = useMutation({
    mutationFn: (entry: ManualTimeEntryInput) => {
      const requestDTO: TaskTimeEntryRequestDTO = {
        startedAt: null,
        endedAt: null,
        minutes: entry.minutes,
        entryDate: entry.entryDate,
        source: "MANUAL",
        note: entry.note,
      };
      return apiLogTaskTimeEntry(taskId as number, requestDTO, getToken);
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey });
      // A manual entry also bumps the task's actualMinutes on the server - invalidate the
      // task lists so the modal's total (read from the task, not from this query) catches up.
      void queryClient.invalidateQueries({ queryKey: ["tasks"] });
    },
    onError: (error) => {
      toast.error(toFriendlyMessage(error, "Unable to save time entry."));
    },
  });

  const deleteEntry = useMutation({
    mutationFn: (entryId: number) => apiDeleteTaskTimeEntry(taskId as number, entryId, getToken),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey });
      void queryClient.invalidateQueries({ queryKey: ["tasks"] });
    },
    onError: (error) => {
      toast.error(toFriendlyMessage(error, "Unable to delete time entry."));
    },
  });

  return {
    entries: query.data ?? [],
    isLoading: query.isLoading,
    addManualEntry: addManualEntry.mutate,
    isAddingManualEntry: addManualEntry.isPending,
    deleteEntry: deleteEntry.mutate,
    isDeletingEntryId: deleteEntry.isPending ? deleteEntry.variables ?? null : null,
  };
}
