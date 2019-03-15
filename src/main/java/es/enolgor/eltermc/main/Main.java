package es.enolgor.eltermc.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Material;

public class Main {
	public static void main(String [] args) throws IOException {
		String materials = Stream.of(Material.values()).filter(material -> material.isItem()).map(material -> "  \""+material.name()+"\"").collect(Collectors.joining(",\n"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("items.json"));
		 writer.write("[\n"+materials+"\n]");
	    writer.close();
	}
}
