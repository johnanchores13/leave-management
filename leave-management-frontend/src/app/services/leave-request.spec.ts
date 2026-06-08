import { TestBed } from '@angular/core/testing';

import { LeaveRequest } from './leave-request';

describe('LeaveRequest', () => {
  let service: LeaveRequest;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LeaveRequest);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
