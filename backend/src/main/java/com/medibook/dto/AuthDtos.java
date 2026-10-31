package com.medibook.dto;
import jakarta.validation.constraints.*;
public final class AuthDtos { private AuthDtos(){}
 public record Register(@NotBlank @Size(max=80) String fullName,@Email @NotBlank String email,@NotBlank @Size(min=10,max=72) String password,@NotBlank String role){}
 public record Login(@Email @NotBlank String email,@NotBlank String password){}
 public record AuthResponse(String token,long userId,String fullName,String email,String role){}
}
