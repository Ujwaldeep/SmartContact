package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println(userName);
		User u =  userRepository.getUserByUserName(userName);
		model.addAttribute("user",u);
		System.out.println(u);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal) { 
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	//open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	//process add contact form 
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal,HttpSession session) {
//			System.out.println("data "+contact);
		try{
		String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			//processing and uploading
			if(file.isEmpty()) {
				contact.setImage("contact.png");
				System.out.println("Image is empty");
			}else {
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			this.userRepository.save(user);
			System.out.println("Added to database");
			
			//message success
			session.setAttribute("message",new Message("Your contact is added !! Add more", "alert-success"));
		
	}catch(Exception e) {
		
		e.printStackTrace();
		//error message
		session.setAttribute("message",new Message("Something went wrong!!", "alert-danger"));
	}
		return "normal/add_contact_form";
	}
	
	//per page n = 5
	//current page = 0
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show Contacts");
		
		//contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
//		user.getContacts();
		int userId =  user.getId();
		
		Pageable pageable = PageRequest.of(page,8);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(userId,pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
		
	}
	
	
	//Showing specific contact detail
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId")Integer cId,Model m,Principal principal) {
		Optional<Contact> contactOpt = this.contactRepository.findById(cId);
		Contact contact = contactOpt.get();
		

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		 
		if(user.getId() == contact.getUser().getId()) {
		m.addAttribute("contact",contact);
		m.addAttribute("title",contact.getName());
		}
		return "normal/contact_detail";
	}
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cId, Model model,Principal principal,HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		//Contact contact = this.contactRepository.findById(cId).get();
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		 
		if(user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
		this.contactRepository.delete(contact);
		
		session.setAttribute("message",new Message("Contact deleted succesfully...","success"));
		
		}
		return "redirect:/user/show-contacts/0";
	}
	
	//open update form
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")Integer cid,Model model) {
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "update_form";
	}
}
