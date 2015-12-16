package hello.controller;

import com.hazelcast.core.IList;
import hello.service.HomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
@Path("/")
public class JerseyTwitterController {

	@Autowired
	private HomerService homerService;


	@Resource
    @Qualifier(value = "tweetCache")
    private IList<Tweet> tweetCache;

    @GET
    public Response getUniqueTweet() {
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(tweetCache.get(0)).build();
    }

    @GET
    @Path("random")
    public Response getRandomTweet() {
        return random()
                .map(tweet -> Response.ok().type(MediaType.APPLICATION_JSON).entity(tweet).build())
                .orElse(Response.serverError().build());
    }

	@GET
	@Path("async")
	public void async(@Suspended final AsyncResponse asyncResponse) {
		CompletableFuture
			.supplyAsync(() -> random()).whenComplete((tweet, throwable) ->  {
				if (tweet.isPresent())
					asyncResponse.resume(Response.ok().type(MediaType.APPLICATION_JSON).entity(tweet).build());
				else
					asyncResponse.resume(Response.serverError().build());
			});
	}

	@GET
	@Path("homer")
	public void homer(@Suspended final AsyncResponse asyncResponse) {
		homerService.process((response, throwable) -> {
			if (throwable == null) {
				asyncResponse.resume(Response.ok().type(MediaType.APPLICATION_JSON).entity(response).build());
			} else {
				asyncResponse.resume(Response.serverError().entity(throwable).build());
			}
		});
	}

    public Optional<Tweet> random() {
        long count = tweetCache.size();
        if(count==0) return Optional.empty();
        Random r = new Random();
        long randomIndex=count<=Integer.MAX_VALUE? r.nextInt((int)count):
                r.longs(1, 0, count).findFirst().orElseThrow(AssertionError::new);
        return Optional.of(tweetCache.get((int) randomIndex));
    }

}
