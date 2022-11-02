package sit.int221.integratedprojectbe.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedprojectbe.dtos.*;
import sit.int221.integratedprojectbe.services.AuthenticationService;
import sit.int221.integratedprojectbe.services.UserService;
import sit.int221.integratedprojectbe.services.imp.UserDetailsServiceImp;
import sit.int221.integratedprojectbe.utils.JwtUtils;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private String token;
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtUtils   jwtUtils;

    @Autowired
    private UserDetailsServiceImp userDetailsServiceImp;

    @Autowired
    private UserService userService;


    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public UserDetailsDTO loadUser(@AuthenticationPrincipal UserDetails userDetails) {
        return authenticationService.loadUserDetailByEmail(userDetails.getUsername());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    public JwtTokenDTO login(@Valid @RequestBody LoginDTO newUser , BindingResult bindingResult) {
        return authenticationService.login(newUser, bindingResult);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsDTO create(@Valid @RequestBody CreateUserDTO newUser, BindingResult bindingResult) {
        return userService.addNewUser(newUser, bindingResult);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<AccessTokenDTO>refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String authToken = request.getHeader("Authorization");
        final String token = authToken.substring(7);
        String username =jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsServiceImp.loadUserByUsername(username);

        if (jwtUtils.canTokenBeRefreshed(token)) {
            String accessToken = jwtUtils.generateToken(userDetails);
            return ResponseEntity.ok(new AccessTokenDTO("refreshed", accessToken));
        }
        else {
            return ResponseEntity.status(401).build();
        }
    }
}
