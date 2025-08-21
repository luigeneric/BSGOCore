package io.github.luigeneric.templates.templates.readers;

import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
abstract class TemplateReader
{
    protected final Path templatePath;
    protected final GsonBuilder gsonBuilder;

    protected TemplateReader(final Path templatePath)
    {
        this.templatePath = templatePath;
        this.gsonBuilder = new GsonBuilder();
        this.registerAllDeserializer();
    }

    protected String readRawTxt(final Path fileName)
    {
        if (fileName == null) return null;
        try(FileReader reader = new FileReader(fileName.toFile()))
        {
            StringBuilder builder = new StringBuilder();
            while (reader.ready())
            {
                builder.append((char) reader.read());
            }
            return builder.toString();
        }
        catch (IOException ex)
        {
            log.error(fileName + ex.getMessage());
        }
        return null;
    }

    protected List<Path> getFilePaths()
    {
        try(Stream<Path> walk = Files.walk(templatePath))
        {
            return walk
                    .filter(f -> f.toString().endsWith(".json"))
                    .filter(f -> !f.toString().contains("!"))
                    .toList();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected abstract void registerAllDeserializer();
}
