package com.Reservation.ApproveService.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenDTO {
    private String sub;
    private String email;
    private String familyName;
}
