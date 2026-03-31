export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: number;
  statusText: string;
  createdDate: string;
  dueDate: string | null;
}

export interface TaskRequest {
  title: string;
  description: string | null;
  status: number;
  dueDate: string | null;
}
