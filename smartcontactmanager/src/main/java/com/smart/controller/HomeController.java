package com.smart.controller;

import java.util.ArrayList;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title","Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	//This handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult r ,@RequestParam(value="agreement",defaultValue = "false") Boolean agreement, Model m,HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed to terms and condition");
				throw new Exception("You have not agreed to terms and condition");
			}
			
			if(r.hasErrors()) {
				System.out.println("has errors");
				m.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setImageUrl("default.png");
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
			
			User result = this.userRepository.save(user);
			
			System.out.println("Result "+result);
			session.setAttribute("message", new Message("Successfully Registered !!","alert-success")); 
			m.addAttribute("user",new User());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			m.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong "+e.getMessage(),"alert-danger"));
		}
		return "signup";
	}
	

	//handler for custorm login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login - Smart Contact Manager");
		return "login";
	}
}
