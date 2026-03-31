import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task, TaskRequest } from '../models/task.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private baseUrl = '/api/tasks';

  constructor(private http: HttpClient) {}

  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/${id}`);
  }

  createTask(request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, request);
  }

  updateTask(id: number, request: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, request);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
