package com.example.demo;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {

	private UserController userController;
	private UserRepository userRepo=mock(UserRepository.class);
	private CartRepository cartRep=mock(CartRepository.class);
	private BCryptPasswordEncoder encoder=mock(BCryptPasswordEncoder.class);
	private CartController cartController;
	private ItemRepository itemRepo=mock(ItemRepository.class);

	@Before
	public void setUp(){
		userController= new UserController();
		TestUtils.injectObjects(userController,"userRepository",userRepo);
		TestUtils.injectObjects(userController,"cartRepository",cartRep);
		TestUtils.injectObjects(userController,"bCryptPasswordEncoder",encoder);
		cartController=new CartController();
		TestUtils.injectObjects(cartController,"itemRepository",itemRepo);
		TestUtils.injectObjects(cartController,"userRepository",userRepo);
		TestUtils.injectObjects(cartController,"cartRepository",cartRep);
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void userControllerTest()throws Exception{
		when(encoder.encode("TestPassword")).thenReturn("thisIsHashed");
		CreateUserRequest createUserRequest=new CreateUserRequest();
		createUserRequest.setUsername("test");
		createUserRequest.setPassword("TestPassword");
		createUserRequest.setConfirmPassword("TestPassword");

		final ResponseEntity<User> response= userController.createUser(createUserRequest);

		assertNotNull(response);
		assertEquals(200,response.getStatusCodeValue());

		User user=response.getBody();
		assertNotNull(user);
		assertEquals(0,user.getId());
		assertEquals("test",user.getUsername());
		assertEquals("thisIsHashed",user.getPassword());

		//testFindByUserName
		when(userRepo.findByUsername("test")).thenReturn(user);
		ResponseEntity<User> findUser=userController.findByUserName("test");
		assertNotNull(findUser);
		assertEquals(200,findUser.getStatusCodeValue());

		User returnedUser=findUser.getBody();
		assertNotNull(returnedUser);
		assertEquals("test",returnedUser.getUsername());
		assertEquals(0,returnedUser.getId());
	}

	@Test
	public void cartControllerTest(){
		User user=new User();
		user.setUsername("test");
		Cart cart=new Cart();

		Item item=new Item();
		item.setId(0L);
		item.setName("Round Widget");
		item.setDescription("A widget that is round");
		item.setPrice(new BigDecimal("2.99"));
		List<Item> itemList=new ArrayList<>();
		itemList.add(item);
		cart.setItems(itemList);
		cart.setUser(user);
		user.setCart(cart);

		ModifyCartRequest modifyCartRequest=new ModifyCartRequest();
		modifyCartRequest.setUsername("test");
		modifyCartRequest.setQuantity(1);
		modifyCartRequest.setItemId(0L);

		when(userRepo.findByUsername("test")).thenReturn(user);
		when(itemRepo.findById(0L)).thenReturn(Optional.of(item));

		ResponseEntity<Cart> response=cartController.addTocart(modifyCartRequest);
		assertNotNull(response);
		assertEquals(200,response.getStatusCodeValue());

		Cart returnedCart=response.getBody();
		Item returnedItem=returnedCart.getItems().get(0);
		assertNotNull(returnedCart);
		assertNotNull(returnedItem);
		assertEquals(user,returnedCart.getUser());
		assertEquals(new BigDecimal("2.99"),returnedCart.getTotal());

		ResponseEntity<Cart> removeRespone=cartController.removeFromcart(modifyCartRequest);
		assertNotNull(removeRespone);
		assertEquals(200,removeRespone.getStatusCodeValue());

		Cart removedCart=removeRespone.getBody();
		List<Item> items=new ArrayList<>();removedCart.getItems();
		assertEquals(0,items.size());
		assertEquals(new BigDecimal("0.00"),removedCart.getTotal());
	}


}
