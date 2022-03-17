import { TestBed } from '@angular/core/testing';

import { TomorrowService } from './tomorrow.service';

describe('TomorrowService', () => {
  let service: TomorrowService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TomorrowService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
