import { TestBed } from '@angular/core/testing';

import { IpinfoService } from './ipinfo.service';

describe('IpinfoService', () => {
  let service: IpinfoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IpinfoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
