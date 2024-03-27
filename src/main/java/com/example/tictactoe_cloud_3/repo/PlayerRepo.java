package com.example.tictactoe_cloud_3.repo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PlayerRepo {
    private Map<UUID, String> dictionary;

    public PlayerRepo(){
        dictionary = new HashMap<>();
    }
    public void addPlayer(UUID uuid, String name){
        dictionary.put(uuid,name);
    }
    public String getPlayer(UUID uuid){
        return dictionary.get(uuid);
    }
    public void deletePlayer(UUID uuid){
        dictionary.remove(uuid);
    }
}
