import { API_ENDPOINTS, authenticatedFetch } from "@/lib/api-config";
import type {
  SubtaskRequestDTO,
  SubtaskResponseDTO,
  TaskReorderItemDTO,
  TaskRequestDTO,
  TaskResponseDTO,
  TaskTimeEntryRequestDTO,
  TaskTimeEntryResponseDTO,
  TimeEntryReflectionRequestDTO,
} from "@/types";

export async function getTasksForDate(
  date: string,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO[]> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}?date=${date}`, { method: 'GET' }, getToken);

  if (response.status === 204) {
    return [];
  }

  if (!response.ok) {
    throw new Error(`Failed to load tasks (${response.status})`);
  }

  return (await response.json()) as TaskResponseDTO[];
}

export async function getTasksForDateRange(
  startDate: string,
  endDate: string,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO[]> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}?startDate=${startDate}&endDate=${endDate}`,
    { method: 'GET' },
    getToken
  );

  if (response.status === 204) {
    return [];
  }

  if (!response.ok) {
    throw new Error(`Failed to load tasks (${response.status})`);
  }

  return (await response.json()) as TaskResponseDTO[];
}

export async function getTaskById(
  id: number,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}/${id}`, { method: 'GET' }, getToken);

  if (!response.ok) {
    throw new Error(`Failed to load task (${response.status})`);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function createTask(
  task: TaskRequestDTO,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(API_ENDPOINTS.tasks, { method: 'POST', body: JSON.stringify(task) }, getToken);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to create task (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function updateTask(
  id: number,
  task: TaskRequestDTO,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}/${id}`, { method: 'PUT', body: JSON.stringify(task) }, getToken);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to update task (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function updateCompletion(
  id: number,
  completed: boolean,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${id}/completion`,
    { method: 'PUT', body: JSON.stringify({ completed }) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to update task (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function updateActualMinutes(
  id: number,
  actualMinutes: number,
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${id}/actual-minutes`,
    { method: 'PUT', body: JSON.stringify({ actualMinutes }) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to save tracked time (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function reorderTasks(
  items: TaskReorderItemDTO[],
  getToken: () => Promise<string | null>
): Promise<TaskResponseDTO[]> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}/reorder`, { method: 'PUT', body: JSON.stringify(items) }, getToken);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to reorder tasks (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO[];
}

export async function deleteTask(id: number, getToken: () => Promise<string | null>): Promise<void> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}/${id}`, { method: 'DELETE' }, getToken);

  if (!response.ok) {
    throw new Error(`Failed to delete task (${response.status})`);
  }
}

export async function archiveTask(id: number, getToken: () => Promise<string | null>): Promise<TaskResponseDTO> {
  const response = await authenticatedFetch(`${API_ENDPOINTS.tasks}/${id}/archive`, { method: 'PUT' }, getToken);

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to archive task (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskResponseDTO;
}

export async function createSubtask(
  taskId: number,
  subtask: SubtaskRequestDTO,
  getToken: () => Promise<string | null>
): Promise<SubtaskResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${taskId}/subtasks`,
    { method: 'POST', body: JSON.stringify(subtask) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to add subtask (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as SubtaskResponseDTO;
}

export async function updateSubtask(
  taskId: number,
  subtaskId: number,
  subtask: SubtaskRequestDTO,
  getToken: () => Promise<string | null>
): Promise<SubtaskResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${taskId}/subtasks/${subtaskId}`,
    { method: 'PUT', body: JSON.stringify(subtask) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to update subtask (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as SubtaskResponseDTO;
}

export async function deleteSubtask(
  taskId: number,
  subtaskId: number,
  getToken: () => Promise<string | null>
): Promise<void> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${taskId}/subtasks/${subtaskId}`,
    { method: 'DELETE' },
    getToken
  );

  if (!response.ok) {
    throw new Error(`Failed to delete subtask (${response.status})`);
  }
}

export async function getTaskTimeEntries(
  taskId: number,
  getToken: () => Promise<string | null>
): Promise<TaskTimeEntryResponseDTO[]> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.tasks}/${taskId}/time-entries`,
    { method: 'GET' },
    getToken
  );

  if (response.status === 204) {
    return [];
  }

  if (!response.ok) {
    throw new Error(`Failed to load time entries (${response.status})`);
  }

  return (await response.json()) as TaskTimeEntryResponseDTO[];
}

export async function getTimeEntriesInRange(
  from: string,
  to: string,
  getToken: () => Promise<string | null>
): Promise<TaskTimeEntryResponseDTO[]> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.timeEntries}?from=${from}&to=${to}`,
    { method: 'GET' },
    getToken
  );

  if (response.status === 204) {
    return [];
  }

  if (!response.ok) {
    throw new Error(`Failed to load time entries (${response.status})`);
  }

  return (await response.json()) as TaskTimeEntryResponseDTO[];
}

export async function logTimeEntry(
  entry: TaskTimeEntryRequestDTO,
  getToken: () => Promise<string | null>
): Promise<TaskTimeEntryResponseDTO> {
  const response = await authenticatedFetch(
    API_ENDPOINTS.timeEntries,
    { method: 'POST', body: JSON.stringify(entry) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to log time entry (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskTimeEntryResponseDTO;
}

export async function updateTimeEntryReflection(
  entryId: number,
  reflection: TimeEntryReflectionRequestDTO,
  getToken: () => Promise<string | null>
): Promise<TaskTimeEntryResponseDTO> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.timeEntries}/${entryId}`,
    { method: 'PATCH', body: JSON.stringify(reflection) },
    getToken
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const message = errorData.message || `Failed to save session reflection (${response.status})`;
    throw new Error(message);
  }

  return (await response.json()) as TaskTimeEntryResponseDTO;
}

export async function deleteTimeEntry(
  entryId: number,
  getToken: () => Promise<string | null>
): Promise<void> {
  const response = await authenticatedFetch(
    `${API_ENDPOINTS.timeEntries}/${entryId}`,
    { method: 'DELETE' },
    getToken
  );

  if (!response.ok) {
    throw new Error(`Failed to delete time entry (${response.status})`);
  }
}
