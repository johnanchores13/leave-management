export interface LeaveRequest {
    requestId: number;
    startDate: string;
    endDate: string;
    leaveType: string;
    requestedQuantity: number;
    leaveStatus: string;
    reason: string;
    rejectionReason?: string;
    readByManager?: boolean;
    readByEmployee?: boolean;
    employee?: any;
}